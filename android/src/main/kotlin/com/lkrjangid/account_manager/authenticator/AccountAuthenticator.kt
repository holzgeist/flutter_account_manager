package com.lkrjangid.account_manager.authenticator

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle

/**
 * Minimal AccountAuthenticator required by Android's AccountManager framework.
 *
 * Custom authentication UI should be implemented by the host app by launching
 * an Activity via the KEY_INTENT returned from addAccount / getAuthToken.
 */
class AccountAuthenticator(private val context: Context) :
    AbstractAccountAuthenticator(context) {

    override fun editProperties(
        response: AccountAuthenticatorResponse,
        accountType: String,
    ): Bundle = Bundle()

    override fun addAccount(
        response: AccountAuthenticatorResponse,
        accountType: String,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle,
    ): Bundle {
        val result = Bundle()
        result.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType)
        result.putString(AccountManager.KEY_ACCOUNT_NAME, "")
        return result
    }

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse,
        account: Account,
        options: Bundle?,
    ): Bundle? = null

    override fun getAuthToken(
        response: AccountAuthenticatorResponse,
        account: Account,
        authTokenType: String,
        options: Bundle,
    ): Bundle {
        val am = AccountManager.get(context)
        val password = am.getPassword(account)

        if (password != null) {
            val result = Bundle()
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
            result.putString(AccountManager.KEY_AUTHTOKEN, password)
            return result
        }

        val result = Bundle()
        result.putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_INVALID_RESPONSE)
        result.putString(AccountManager.KEY_ERROR_MESSAGE, "No credentials available")
        return result
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
