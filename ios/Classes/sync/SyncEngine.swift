import Foundation

/// Performs the actual sync operation over the network using URLSession.
///
/// This is a plugin-level default implementation. Apps that need custom
/// sync logic should replace this class or extend it.
class SyncEngine {

    private var currentTask: Task<SyncResultData, Error>?

    // MARK: - Public API

    /// Perform a sync for the given account.
    ///
    /// - Parameters:
    ///   - account: The account to sync.
    ///   - expedited: When `true`, the sync should be treated as high priority.
    /// - Returns: A `SyncResultData` describing the outcome.
    func performSync(account: AccountData, expedited: Bool) async throws -> SyncResultData {
        let task = Task<SyncResultData, Error> {
            return try await runSync(account: account, expedited: expedited)
        }
        currentTask = task
        defer { currentTask = nil }
        return try await task.value
    }

    /// Cancel any in-progress sync task.
    func cancelCurrentSync() {
        currentTask?.cancel()
        currentTask = nil
    }

    // MARK: - Private

    private func runSync(account: AccountData, expedited: Bool) async throws -> SyncResultData {
        let startTime = Date()

        // Guard against empty account info — nothing to sync.
        guard !account.accountType.isEmpty else {
            return SyncResultData(
                success: true,
                errorCode: nil,
                errorMessage: nil,
                stats: SyncStatsData(
                    itemsUploaded: 0,
                    itemsDownloaded: 0,
                    conflicts: 0,
                    syncTimeMs: 0
                )
            )
        }

        // Honour cooperative cancellation before doing network work.
        try Task.checkCancellation()

        let configuration: URLSessionConfiguration = expedited
            ? .ephemeral
            : .default
        configuration.timeoutIntervalForRequest = expedited ? 15 : 60
        let session = URLSession(configuration: configuration)

        // ------------------------------------------------------------------
        // Real sync logic would build requests against the account's endpoint
        // here. The implementation below is a best-effort no-op that validates
        // the network is reachable without uploading or downloading anything.
        // Replace this section with your application-specific sync calls.
        // ------------------------------------------------------------------

        var itemsUploaded: Int64 = 0
        var itemsDownloaded: Int64 = 0

        if let syncURLString = account.userData?["sync_endpoint"],
           let syncURL = syncURLString.flatMap({ URL(string: $0) }) {

            try Task.checkCancellation()

            var request = URLRequest(url: syncURL)
            request.httpMethod = "HEAD"

            let (_, response) = try await session.data(for: request)

            if let httpResponse = response as? HTTPURLResponse,
               !(200..<300).contains(httpResponse.statusCode) {
                return SyncResultData(
                    success: false,
                    errorCode: Int64(httpResponse.statusCode),
                    errorMessage: HTTPURLResponse.localizedString(forStatusCode: httpResponse.statusCode),
                    stats: nil
                )
            }

            // Placeholder counts — replace with real upload/download tracking.
            itemsUploaded = 0
            itemsDownloaded = 0
        }

        let elapsedMs = Int64(Date().timeIntervalSince(startTime) * 1000)

        return SyncResultData(
            success: true,
            errorCode: nil,
            errorMessage: nil,
            stats: SyncStatsData(
                itemsUploaded: itemsUploaded,
                itemsDownloaded: itemsDownloaded,
                conflicts: 0,
                syncTimeMs: elapsedMs
            )
        )
    }
}
