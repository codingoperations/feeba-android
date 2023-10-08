package io.feeba

import android.app.Application
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import io.feeba.data.FeebaConfig
import io.feeba.data.LocalStateHolder
import io.feeba.data.UserData
import io.feeba.lifecycle.ActivityLifecycleListener
import io.feeba.lifecycle.ApplicationLifecycleObserver
import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger
import io.least.core.ServerConfig

object Feeba {
    lateinit var app: Application
    val appContext: Context
        get() = app.applicationContext

    lateinit var config: FeebaConfig
    private lateinit var localStateHolder: LocalStateHolder
    private var isInitialized = false

    fun init(app: Application, serverConfig: ServerConfig) {
        if (isInitialized) {
            Logger.log(LogLevel.WARN, "Feeba already initialized")
            return
        }
        localStateHolder = LocalStateHolder(app)
        this.app = app
        config = FeebaConfig(serverConfig)
        ProcessLifecycleOwner.get().lifecycle.addObserver(ApplicationLifecycleObserver(localStateHolder))
        app.registerActivityLifecycleCallbacks(ActivityLifecycleListener())
    }

    fun login(userId: String, email: String? = null, phoneNumber: String? = null) {
        Logger.log(LogLevel.DEBUG, "login -> $userId, $email, $phoneNumber")
        localStateHolder.login(UserData(userId, email, phoneNumber))
    }

    fun logout() {
        Logger.log(LogLevel.DEBUG, "logout")
        localStateHolder.logout()
    }

    fun onEvent(eventName: String, value: String? = null) {
        Logger.log(LogLevel.DEBUG, "onEvent -> $eventName, value: $value")
        localStateHolder.onEvent(eventName)
        // check if we have a survey for this event
        // check if conditions are met
    }

    fun showConditionalSurvey() {
        println("Feeba showConditionalSurvey")
    }

}
