package com.lkrjangid.account_manager.sync

import android.app.Service
import android.content.Intent
import android.os.IBinder

/** Service that exposes the [SyncAdapter] IBinder to the system. */
class SyncService : Service() {

    companion object {
        private val lock = Any()
        private var syncAdapter: SyncAdapter? = null
    }

    override fun onCreate() {
        super.onCreate()
        synchronized(lock) {
            if (syncAdapter == null) {
                syncAdapter = SyncAdapter(applicationContext, true)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder = syncAdapter!!.syncAdapterBinder
}
