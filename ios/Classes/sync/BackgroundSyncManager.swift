import Foundation
import BackgroundTasks

/// Manages background sync scheduling using BGTaskScheduler (iOS 13+).
///
/// To use, the host app must declare the background task identifier
/// "dev.flutter.account_manager.periodic_sync" in its Info.plist under
/// "Permitted background task scheduler identifiers".
class BackgroundSyncManager {

    static let shared = BackgroundSyncManager()

    private let taskIdentifier = "dev.flutter.account_manager.periodic_sync"

    /// The interval (seconds) to use when rescheduling. Persisted across calls.
    private var scheduledIntervalSeconds: TimeInterval = 15 * 60
    private var requiresNetwork: Bool = true
    private var requiresCharging: Bool = false

    private init() {}

    // MARK: - Registration

    /// Must be called during app launch (e.g. from the Flutter plugin's `register(with:)`).
    func registerBackgroundTasks() {
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: taskIdentifier,
            using: nil
        ) { [weak self] task in
            self?.handleBackgroundTask(task as! BGProcessingTask)
        }
    }

    // MARK: - Scheduling

    /// Schedule (or reschedule) periodic background sync using the supplied config.
    func schedulePeriodicSync(account: AccountData, config: PeriodicSyncConfig) throws {
        scheduledIntervalSeconds = TimeInterval(config.intervalSeconds)
        requiresNetwork = config.requiresNetwork ?? true
        requiresCharging = config.requiresCharging ?? false
        try scheduleNextSync()
    }

    /// Cancel any pending background sync task.
    func cancelPeriodicSync() {
        BGTaskScheduler.shared.cancel(taskRequestWithIdentifier: taskIdentifier)
    }

    // MARK: - Private helpers

    private func scheduleNextSync() throws {
        let request = BGProcessingTaskRequest(identifier: taskIdentifier)
        request.earliestBeginDate = Date(timeIntervalSinceNow: scheduledIntervalSeconds)
        request.requiresNetworkConnectivity = requiresNetwork
        request.requiresExternalPower = requiresCharging
        do {
            try BGTaskScheduler.shared.submit(request)
        } catch BGTaskScheduler.Error.notPermitted {
            // The task identifier is not registered in Info.plist — surface this
            // as a descriptive error so developers know what to add.
            throw BackgroundSyncError.notPermitted(
                "Background task identifier '\(taskIdentifier)' is not listed under " +
                "'BGTaskSchedulerPermittedIdentifiers' in the app's Info.plist."
            )
        } catch {
            throw error
        }
    }

    private func handleBackgroundTask(_ task: BGProcessingTask) {
        // Reschedule before doing work so the next run is queued even if this
        // one is expiring.
        try? scheduleNextSync()

        let syncEngine = SyncEngine()

        task.expirationHandler = {
            syncEngine.cancelCurrentSync()
        }

        // Use a placeholder AccountData for the background run; real
        // implementations would persist the last-synced account.
        let account = AccountData(username: "", accountType: "")

        Task {
            do {
                _ = try await syncEngine.performSync(account: account, expedited: false)
                task.setTaskCompleted(success: true)
            } catch {
                task.setTaskCompleted(success: false)
            }
        }
    }
}

// MARK: - Errors

enum BackgroundSyncError: LocalizedError {
    case notPermitted(String)

    var errorDescription: String? {
        switch self {
        case .notPermitted(let msg): return msg
        }
    }
}
