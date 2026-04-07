package com.lkrjangid.account_manager.sync

import com.lkrjangid.account_manager.SyncCallbackFlutterApi

/** Singleton that holds the [SyncCallbackFlutterApi] instance registered by the plugin. */
object SyncCallbackManager {
    private var callbackApi: SyncCallbackFlutterApi? = null

    fun register(api: SyncCallbackFlutterApi) {
        callbackApi = api
    }

    fun unregister() {
        callbackApi = null
    }

    fun getCallbackApi(): SyncCallbackFlutterApi? = callbackApi
}
