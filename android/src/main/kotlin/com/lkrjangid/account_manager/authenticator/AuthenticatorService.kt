package com.lkrjangid.account_manager.authenticator

import android.app.Service
import android.content.Intent
import android.os.IBinder

/** Service that exposes the [AccountAuthenticator] IBinder to the system. */
class AuthenticatorService : Service() {

    private lateinit var authenticator: AccountAuthenticator

    override fun onCreate() {
        super.onCreate()
        authenticator = AccountAuthenticator(this)
    }

    override fun onBind(intent: Intent): IBinder = authenticator.iBinder
}
