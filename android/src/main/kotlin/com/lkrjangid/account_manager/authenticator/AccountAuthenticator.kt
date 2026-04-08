package com.lkrjangid.account_manager.authenticator

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle

/**
 * AccountAuthenticator wired to Android's AccountManager framework.
 *
 * Key contract:
 * - [addAccount] must return [AccountManager.KEY_INTENT] pointing to
 *   [AddAccountActivity] so Settings → Add account actually opens a UI.
 * - [getAuthToken] returns a cached token or an Intent to re-authenticate.
 */
class AccountAuthenticator(private val context: Context) :
    AbstractAccountAuthenticator(context) {

    override fun editProperties(
        response: AccountAuthenticatorResponse,
        accountType: String,
    ): Bundle = Bundle()

    /**
     * Called by Android when the user taps "Add account" in Settings.
     * Returns an Intent so Android knows which Activity to launch.
     * Without KEY_INTENT, nothing happens when the user taps the entry.
     */
    override fun addAccount(
        response: AccountAuthenticatorResponse,
        accountType: String,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle,
    ): Bundle {
        val intent = Intent(context, AddAccountActivity::class.java).apply {
            putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType)
            // Pass the response so AddAccountActivity can call setAccountAuthenticatorResult()
            putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            // Ensure the Activity starts fresh from Settings
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return Bundle().apply {
            putParcelable(AccountManager.KEY_INTENT, intent)
        }
    }

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse,
        account: Account,
        options: Bundle?,
    ): Bundle? = null

    /**
     * Returns a cached token if available.
     * When no credentials exist, returns KEY_INTENT so the caller can
     * re-authenticate via [AddAccountActivity].
     */
    override fun getAuthToken(
        response: AccountAuthenticatorResponse,
        account: Account,
        authTokenType: String,
        options: Bundle,
    ): Bundle {
        val am = AccountManager.get(context)
        val password = am.getPassword(account)

        return if (password != null) {
            Bundle().apply {
                putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
                putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
                putString(AccountManager.KEY_AUTHTOKEN, password)
            }
        } else {
            // No credentials — launch re-auth activity
            val intent = Intent(context, AddAccountActivity::class.java).apply {
                putExtra(AccountManager.KEY_ACCOUNT_TYPE, account.type)
                putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            Bundle().apply {
                putParcelable(AccountManager.KEY_INTENT, intent)
            }
        }
    }

    override fun getAuthTokenLabel(authTokenType: String): String = authTokenType

    override fun updateCredentials(
        response: AccountAuthenticatorResponse,
        account: Account,
        authTokenType: String?,
        options: Bundle?,
    ): Bundle? = null

    override fun hasFeatures(
        response: AccountAuthenticatorResponse,
        account: Account,
        features: Array<out String>,
    ): Bundle = Bundle().apply { putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false) }
}
