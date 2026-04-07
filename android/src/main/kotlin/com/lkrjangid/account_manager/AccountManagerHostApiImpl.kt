package com.lkrjangid.account_manager

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import com.lkrjangid.account_manager.sync.SyncManager
import com.lkrjangid.account_manager.utils.toAccountData
import com.lkrjangid.account_manager.utils.toAndroidAccount
import com.lkrjangid.account_manager.utils.toBundle
import kotlinx.coroutines.*

/** Implements [AccountManagerHostApi] using Android's AccountManager system service. */
class AccountManagerHostApiImpl(
    private val context: Context,
    private val syncManager: SyncManager,
) : AccountManagerHostApi {

    private val accountManager: AccountManager = AccountManager.get(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun dispose() {
        scope.cancel()
    }

    // -------------------------------------------------------------------------
    // Account Operations
    // -------------------------------------------------------------------------

    override fun addAccount(
        account: AccountData,
        password: String,
        callback: (Result<Boolean>) -> Unit,
    ) {
        scope.launch {
            try {
                val androidAccount = account.toAndroidAccount()
                val success = accountManager.addAccountExplicitly(
                    androidAccount,
                    password,
                    account.userData?.toBundle(),
                )
                if (success) {
                    account.displayName?.let {
                        accountManager.setUserData(androidAccount, "displayName", it)
                    }
                    val authority = "${account.accountType}.provider"
                    ContentResolver.setSyncAutomatically(androidAccount, authority, true)
                }
                withContext(Dispatchers.Main) { callback(Result.success(success)) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(Result.failure(e)) }
            }
        }
    }

    override fun getAccounts(
        accountType: String,
        callback: (Result<List<AccountData>>) -> Unit,
    ) {
        scope.launch {
            try {
                val accounts = accountManager.getAccountsByType(accountType)
                val list = accounts.map { it.toAccountData(accountManager) }
                withContext(Dispatchers.Main) { callback(Result.success(list)) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(Result.failure(e)) }
            }
        }
    }

    override fun getAccount(
        username: String,
        accountType: String,
        callback: (Result<AccountData?>) -> Unit,
    ) {
        scope.launch {
            try {
                val found = accountManager.getAccountsByType(accountType)
                    .firstOrNull { it.name == username }
                    ?.toAccountData(accountManager)
                withContext(Dispatchers.Main) { callback(Result.success(found)) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(Result.failure(e)) }
            }
        }
    }

    override fun updateAccount(
        account: AccountData,
        callback: (Result<Boolean>) -> Unit,
    ) {
        scope.launch {
            try {
                val androidAccount = account.toAndroidAccount()
                account.displayName?.let {
                    accountManager.setUserData(androidAccount, "displayName", it)
                }
                account.userData?.forEach { (k, v) ->
                    if (k != null) accountManager.setUserData(androidAccount, k, v)
                }
                withContext(Dispatchers.Main) { callback(Result.success(true)) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(Result.failure(e)) }
            }
        }
    }

    override fun removeAccount(
        account: AccountData,
        callback: (Result<Boolean>) -> Unit,
    ) {
        scope.launch {
            try {
                val androidAccount = account.toAndroidAccount()
                @Suppress("DEPRECATION")
                val success = accountManager.removeAccountExplicitly(androidAccount)
                withContext(Dispatchers.Main) { callback(Result.success(success)) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(Result.failure(e)) }
            }
        }
    }

    override fun accountExists(
        username: String,
        accountType: String,
        callback: (Result<Boolean>) -> Unit,
    ) {
        scope.launch {
            try {
                val exists = accountManager.getAccountsByType(accountType)
                    .any { it.name == username }
                withContext(Dispatchers.Main) { callback(Result.success(exists)) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(Result.failure(e)) }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Credential Operations
    // -------------------------------------------------------------------------

    override fun updateCredentials(
        account: AccountData,
        newPassword: String,
        callback: (Result<Boolean>) -> Unit,
    ) {
        scope.launch {
            try {
                val androidAccount = account.toAndroidAccount()
                accountManager.setPassword(androidAccount, newPassword)
                withContext(Dispatchers.Main) { callback(Result.success(true)) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(Result.failure(e)) }
            }
        }
    }

    override fun validateCredentials(
        username: String,
        password: String,
        accountType: String,
        callback: (Result<Boolean>) -> Unit,
    ) {
        scope.launch {
            try {
                val androidAccount = Account(username, accountType)
                val stored = accountManager.getPassword(androidAccount)
                withContext(Dispatchers.Main) {
                    callback(Result.success(stored != null && stored == password))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(Result.failure(e)) }
            }
        }
    }

    override fun clearCredentials(
        account: AccountData,
        callback: (Result<Boolean>) -> Unit,
    ) {
        scope.launch {
            try {
                val androidAccount = account.toAndroidAccount()
                accountManager.setPassword(androidAccount, null)
                withContext(Dispatchers.Main) { callback(Result.success(true)) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(Result.failure(e)) }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Auth Token Operations
    // -------------------------------------------------------------------------

    override fun getAuthToken(
        account: AccountData,
        tokenType: String,
        callback: (Result<AuthTokenResult>) -> Unit,
    ) {
        scope.launch {
            try {
                val androidAccount = account.toAndroidAccount()
                val cached = accountManager.peekAuthToken(androidAccount, tokenType)
                if (cached != null) {
                    withContext(Dispatchers.Main) {
                        callback(Result.success(AuthTokenResult(token = cached)))
                    }
                    return@launch
                }
                val future = accountManager.getAuthToken(
                    androidAccount, tokenType, null, false, null, null
                )
                val bundle = future.result
                val token = bundle.getString(AccountManager.KEY_AUTHTOKEN)
                withContext(Dispatchers.Main) {
                    if (token != null) {
                        callback(Result.success(AuthTokenResult(token = token)))
                    } else {
                        callback(
                            Result.success(
                                AuthTokenResult(
                                    errorCode = -1L,
                                    errorMessage = "No token available",
                                    requiresUserInteraction =
                                        bundle.containsKey(AccountManager.KEY_INTENT),
                                )
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(
                        Result.success(
                            AuthTokenResult(errorCode = -1L, errorMessage = e.message)
                        )
                    )
                }
            }
        }
    }

    override fun setAuthToken(
        account: AccountData,
        tokenType: String,
        token: String,
        callback: (Result<Boolean>) -> Unit,
    ) {
        scope.launch {
            try {
                val androidAccount = account.toAndroidAccount()
                accountManager.setAuthToken(androidAccount, tokenType, token)
                withContext(Dispatchers.Main) { callback(Result.success(true)) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(Result.failure(e)) }
            }
        }
    }

    override fun invalidateAuthToken(
        accountType: String,
        token: String,
        callback: (Result<Boolean>) -> Unit,
    ) {
        scope.launch {
            try {
                accountManager.invalidateAuthToken(accountType, token)
                withContext(Dispatchers.Main) { callback(Result.success(true)) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(Result.failure(e)) }
            }
        }
    }

    override fun invalidateAllTokens(
        account: AccountData,
        tokenType: String,
        callback: (Result<Boolean>) -> Unit,
    ) {
        scope.launch {
            try {
                val androidAccount = account.toAndroidAccount()
                val token = accountManager.peekAuthToken(androidAccount, tokenType)
                if (token != null) {
                    accountManager.invalidateAuthToken(account.accountType, token)
                }
                withContext(Dispatchers.Main) { callback(Result.success(true)) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(Result.failure(e)) }
            }
        }
    }

    override fun getAvailableTokenTypes(
        account: AccountData,
        callback: (Result<List<String>>) -> Unit,
    ) {
        scope.launch {
            try {
                // Android doesn't expose available token types natively; return empty list.
                withContext(Dispatchers.Main) { callback(Result.success(emptyList())) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(Result.failure(e)) }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Sync Operations
    // -------------------------------------------------------------------------

    override fun syncNow(
        account: AccountData,
        expedited: Boolean,
        callback: (Result<SyncResultData>) -> Unit,
    ) {
        scope.launch {
            try {
                val result = syncManager.requestSync(account.username, account.accountType, expedited)
                withContext(Dispatchers.Main) { callback(Result.success(result)) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(
                        Result.success(
                            SyncResultData(success = false, errorCode = -1L, errorMessage = e.message)
                        )
                    )
                }
            }
        }
    }

    override fun setSyncAutomatically(
        account: AccountData,
        enabled: Boolean,
        callback: (Result<Boolean>) -> Unit,
    ) {
        scope.launch {
            try {
                val androidAccount = account.toAndroidAccount()
                val authority = "${account.accountType}.provider"
                ContentResolver.setSyncAutomatically(androidAccount, authority, enabled)
                withContext(Dispatchers.Main) { callback(Result.success(true)) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(Result.failure(e)) }
            }
        }
    }

    override fun isSyncAutomatically(
        account: AccountData,
        callback: (Result<Boolean>) -> Unit,
    ) {
        scope.launch {
            try {
                val androidAccount = account.toAndroidAccount()
                val authority = "${account.accountType}.provider"
                val result = ContentResolver.getSyncAutomatically(androidAccount, authority)
                withContext(Dispatchers.Main) { callback(Result.success(result)) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(Result.failure(e)) }
            }
        }
    }

    override fun addPeriodicSync(
        account: AccountData,
        config: PeriodicSyncConfig,
        callback: (Result<Boolean>) -> Unit,
    ) {
        scope.launch {
            try {
                val androidAccount = account.toAndroidAccount()
                val authority = "${account.accountType}.provider"
                ContentResolver.removePeriodicSync(androidAccount, authority, Bundle.EMPTY)
                ContentResolver.addPeriodicSync(
                    androidAccount,
                    authority,
                    config.extras?.toBundle() ?: Bundle.EMPTY,
                    config.intervalSeconds.toLong(),
                )
                withContext(Dispatchers.Main) { callback(Result.success(true)) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(Result.failure(e)) }
            }
        }
    }

    override fun removePeriodicSync(
        account: AccountData,
        callback: (Result<Boolean>) -> Unit,
    ) {
        scope.launch {
            try {
                val androidAccount = account.toAndroidAccount()
                val authority = "${account.accountType}.provider"
                ContentResolver.removePeriodicSync(androidAccount, authority, Bundle.EMPTY)
                withContext(Dispatchers.Main) { callback(Result.success(true)) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(Result.failure(e)) }
            }
        }
    }

    override fun getSyncStatus(
        account: AccountData,
        callback: (Result<SyncStatus>) -> Unit,
    ) {
        scope.launch {
            try {
                val status = when {
                    syncManager.isSyncActive(account.username, account.accountType) -> SyncStatus.ACTIVE
                    syncManager.isSyncPending(account.username, account.accountType) -> SyncStatus.PENDING
                    else -> SyncStatus.IDLE
                }
                withContext(Dispatchers.Main) { callback(Result.success(status)) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(Result.failure(e)) }
            }
        }
    }

    override fun cancelSync(
        account: AccountData,
        callback: (Result<Boolean>) -> Unit,
    ) {
        scope.launch {
            try {
                syncManager.cancelSync(account.username, account.accountType)
                withContext(Dispatchers.Main) { callback(Result.success(true)) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(Result.failure(e)) }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Platform-Specific
    // -------------------------------------------------------------------------

    override fun openAccountSettings(callback: (Result<Boolean>) -> Unit) {
        try {
            val intent = Intent(Settings.ACTION_SYNC_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            callback(Result.success(true))
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    override fun isConfigured(callback: (Result<Boolean>) -> Unit) {
        callback(Result.success(true))
    }

    override fun getPlatformCapabilities(callback: (Result<Map<String, Boolean>>) -> Unit) {
        val caps = mapOf(
            "systemAccountSettings" to true,
            "backgroundSync" to true,
            "keychainStorage" to false,
            "cloudKitSync" to false,
            "pushNotificationSync" to false,
            "biometricAuth" to false,
        )
        callback(Result.success(caps))
    }
}
