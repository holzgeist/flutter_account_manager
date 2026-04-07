import Foundation

private let kAccountsKey = "com.lkrjangid.account_manager.accounts"

/// Stores account metadata using UserDefaults with JSON encoding.
class AccountStore {

    static let shared = AccountStore()
    private init() {}

    private let defaults = UserDefaults.standard

    // MARK: - Internal Model

    struct StoredAccount: Codable {
        let username: String
        let accountType: String
        var displayName: String?
        var userData: [String: String]?
        let createdAt: Date
        var updatedAt: Date
        var syncEnabled: Bool
    }

    // MARK: - CRUD

    func saveAccount(_ account: AccountData) throws {
        var all = loadAll()
        let key = accountKey(username: account.username, accountType: account.accountType)
        let stored = StoredAccount(
            username: account.username,
            accountType: account.accountType,
            displayName: account.displayName,
            userData: account.userData?.compactMapValues { $0 },
            createdAt: all[key]?.createdAt ?? Date(),
            updatedAt: Date(),
            syncEnabled: true
        )
        all[key] = stored
        try persist(all)
    }

    func getAccounts(ofType accountType: String) throws -> [AccountData] {
        return loadAll().values
            .filter { $0.accountType == accountType }
            .map { toAccountData($0) }
    }

    func getAccount(username: String, accountType: String) throws -> AccountData? {
        let key = accountKey(username: username, accountType: accountType)
        guard let stored = loadAll()[key] else { return nil }
        return toAccountData(stored)
    }

    func updateAccount(_ account: AccountData) throws {
        var all = loadAll()
        let key = accountKey(username: account.username, accountType: account.accountType)
        guard var stored = all[key] else { return }
        stored.displayName = account.displayName
        stored.userData = account.userData?.compactMapValues { $0 }
        stored.updatedAt = Date()
        all[key] = stored
        try persist(all)
    }

    func removeAccount(username: String, accountType: String) throws {
        var all = loadAll()
        let key = accountKey(username: username, accountType: accountType)
        all.removeValue(forKey: key)
        try persist(all)
    }

    func accountExists(username: String, accountType: String) -> Bool {
        let key = accountKey(username: username, accountType: accountType)
        return loadAll()[key] != nil
    }

    // MARK: - Private

    private func accountKey(username: String, accountType: String) -> String {
        "\(accountType):\(username)"
    }

    private func loadAll() -> [String: StoredAccount] {
        guard let data = defaults.data(forKey: kAccountsKey),
              let decoded = try? JSONDecoder().decode([String: StoredAccount].self, from: data) else {
            return [:]
        }
        return decoded
    }

    private func persist(_ accounts: [String: StoredAccount]) throws {
        let data = try JSONEncoder().encode(accounts)
        defaults.set(data, forKey: kAccountsKey)
    }

    private func toAccountData(_ stored: StoredAccount) -> AccountData {
        AccountData(
            username: stored.username,
            accountType: stored.accountType,
            displayName: stored.displayName,
            userData: stored.userData
        )
    }
}
