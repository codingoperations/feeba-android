package io.feeba

import android.app.Application
import android.content.Context
import io.feeba.data.FeebaResponse
import io.feeba.data.LocalStateHolder
import io.feeba.data.sql.AndroidStateStorage
import io.feeba.lifecycle.AndroidLifecycleManager
import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger
import io.least.core.ServerConfig

object Feeba {
    lateinit var app: Application
    private var isInitialized = false
    val User = FeebaFacade.User

    val appContext: Context
        get() = app.applicationContext

    fun init(app: Application, serverConfig: ServerConfig) {
        if (isInitialized) {
            Logger.log(LogLevel.WARN, "Feeba already initialized. Ignoring init call.")
            return
        }
        this.app = app
        val localStateHolder = LocalStateHolder(AndroidStateStorage(appContext))
        FeebaFacade.init(
            serverConfig.copy(hostUrl = serverConfig.hostUrl ?: "https://api.feeba.io"),
            localStateHolder,
            StateManager(AndroidLifecycleManager(app), localStateHolder)
        )
    }

    fun triggerEvent(eventName: String, value: String? = null) {
        FeebaFacade.triggerEvent(eventName, value)
    }

    fun pageOpened(pageName: String) {
        FeebaFacade.pageOpened(pageName)
    }

    fun pageClosed(pageName: String) {
        FeebaFacade.pageClosed(pageName)
    }

    fun fetchFeebaConfig(): FeebaResponse? {
        return FeebaFacade.feebaResponse()
    }
}