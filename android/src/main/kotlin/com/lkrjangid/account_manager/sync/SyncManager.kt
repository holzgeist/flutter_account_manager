package com.lkrjangid.account_manager.sync

import android.accounts.Account
import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import com.lkrjangid.account_manager.SyncResultData
import com.lkrjangid.account_manager.SyncStatsData

private const val PROVIDER_AUTHORITY_SUFFIX = ".provider"

/** Manages sync scheduling and manual sync requests. */
class SyncManager(private val context: Context) {

    fun requestSync(username: String, accountType: String, expedited: Boolean): SyncResultData {
        val account = Account(username, accountType)
        val authority = accountType + PROVIDER_AUTHORITY_SUFFIX

        val extras = Bundle().apply {
            putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
            if (expedited) {
                putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
            }
        }

        ContentResolver.requestSync(account, authority, extras)

        return SyncResultData(
            success = true,
            stats = SyncStatsData(
                itemsUploaded = 0,
                itemsDownloaded = 0,
                conflicts = 0,
                syncTimeMs = 0,
            ),
        )
    }

    fun isSyncActive(username: String, accountType: String): Boolean {
        val account = Account(username, accountType)
        val authority = accountType + PROVIDER_AUTHORITY_SUFFIX
        return ContentResolver.isSyncActive(account, authority)
    }

    fun isSyncPending(username: String, accountType: String): Boolean {
        val account = Account(username, accountType)
        val authority = accountType + PROVIDER_AUTHORITY_SUFFIX
        return ContentResolver.isSyncPending(account, authority)
    }

    fun cancelSync(username: String, accountType: String) {
        val account = Account(username, accountType)
        val authority = accountType + PROVIDER_AUTHORITY_SUFFIX
        ContentResolver.cancelSync(account, authority)
    }
}
