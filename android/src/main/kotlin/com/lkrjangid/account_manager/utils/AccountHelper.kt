package com.lkrjangid.account_manager.utils

import android.accounts.Account
import android.accounts.AccountManager
import android.os.Bundle
import com.lkrjangid.account_manager.AccountData

/** Extension helpers for converting between Android Account and Pigeon AccountData. */

fun Account.toAccountData(accountManager: AccountManager): AccountData {
    val displayName = accountManager.getUserData(this, "displayName")
    val userDataMap = mutableMapOf<String?, String?>()
    userDataMap["displayName"] = displayName
    return AccountData(
        username = this.name,
        accountType = this.type,
        displayName = displayName,
        userData = userDataMap,
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
