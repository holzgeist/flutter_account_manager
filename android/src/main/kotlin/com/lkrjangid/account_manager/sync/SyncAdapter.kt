package com.lkrjangid.account_manager.sync

import android.accounts.Account
import android.accounts.AccountManager
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.SyncResult
import android.os.Bundle
import com.lkrjangid.account_manager.SyncProgressData
import com.lkrjangid.account_manager.SyncResultData
import com.lkrjangid.account_manager.SyncStatsData
import com.lkrjangid.account_manager.utils.toAccountData

/**
 * SyncAdapter performs battery-efficient background synchronisation using
 * Android's SyncFramework. Callbacks are forwarded to Flutter via
 * [SyncCallbackManager].
 */
class SyncAdapter(
    context: Context,
    autoInitialize: Boolean,
    allowParallelSyncs: Boolean = false,
) : AbstractThreadedSyncAdapter(context, autoInitialize, allowParallelSyncs) {

    override fun onPerformSync(
        account: Account,
        extras: Bundle,
        authority: String,
        provider: ContentProviderClient,
        syncResult: SyncResult,
    ) {
        val am = AccountManager.get(context)
        val accountData = account.toAccountData(am)
        val callbackApi = SyncCallbackManager.getCallbackApi()

        callbackApi?.onSyncStarted(accountData) {}

        try {
            val startMs = System.currentTimeMillis()

            // Phase 1: Upload
            callbackApi?.onSyncProgress(
                accountData,
                SyncProgressData(phase = "uploading", progress = 0.0, message = "Uploading local changes…"),
            ) {}
            val uploaded = performUpload(account, provider)

            // Phase 2: Download
            callbackApi?.onSyncProgress(
                accountData,
                SyncProgressData(phase = "downloading", progress = 0.5, message = "Downloading remote changes…"),
            ) {}
            val downloaded = performDownload(account, provider)

            // Phase 3: Conflict resolution
            callbackApi?.onSyncProgress(
                accountData,
                SyncProgressData(phase = "resolving_conflicts", progress = 0.9, message = "Resolving conflicts…"),
            ) {}
            val conflicts = resolveConflicts(account, provider)

            val elapsedMs = System.currentTimeMillis() - startMs
            callbackApi?.onSyncCompleted(
                accountData,
                SyncResultData(
                    success = true,
                    stats = SyncStatsData(
                        itemsUploaded = uploaded.toLong(),
                        itemsDownloaded = downloaded.toLong(),
                        conflicts = conflicts.toLong(),
                        syncTimeMs = elapsedMs,
                    ),
                ),
            ) {}
        } catch (e: Exception) {
            syncResult.stats.numIoExceptions++
            callbackApi?.onSyncCompleted(
                accountData,
                SyncResultData(
                    success = false,
                    errorCode = -1L,
                    errorMessage = e.message,
                ),
            ) {}
        }
    }

    private fun performUpload(account: Account, provider: ContentProviderClient): Int = 0

    private fun performDownload(account: Account, provider: ContentProviderClient): Int = 0

    private fun resolveConflicts(account: Account, provider: ContentProviderClient): Int = 0
}
