package com.lkrjangid.account_manager.utils

import android.accounts.Account
import android.accounts.AccountManager
import android.os.Bundle
import com.lkrjangid.account_manager.AccountData

/** Extension helpers for converting between Android Account and Pigeon AccountData. */

fun Account.toAccountData(accountManager: AccountManager): AccountData {
    val displayName = accountManager.getUserData(this, "displayName")
    // Only include entries with non-null values to avoid cast failures on the Dart side.
    val userDataMap = mutableMapOf<String?, String?>()
    if (displayName != null) userDataMap["displayName"] = displayName
    return AccountData(
        username = this.name,
        accountType = this.type,
        displayName = displayName,
        userData = if (userDataMap.isEmpty()) null else userDataMap,
    )
}

fun Map<String?, String?>.toBundle(): Bundle {
    val bundle = Bundle()
    forEach { (key, value) ->
        if (key != null) bundle.putString(key, value)
    }
    return bundle
}

fun AccountData.toAndroidAccount(): Account = Account(username, accountType)
