package io.feeba

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import io.feeba.data.FeebaResponse
import io.feeba.data.LocalStateHolder
import io.feeba.lifecycle.ActivityLifecycleListener
import io.feeba.lifecycle.ApplicationLifecycleObserver
import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger
import io.least.core.ServerConfig

object Feeba {
    private lateinit var localStateHolder: LocalStateHolder
    private var isInitialized = false

    fun init(app: Application) {
        if (isInitialized) {
            Logger.log(LogLevel.WARN, "Feeba already initialized")
            return
        }
        localStateHolder  = LocalStateHolder(app)
        ProcessLifecycleOwner.get().lifecycle.addObserver(ApplicationLifecycleObserver(localStateHolder))
        app.registerActivityLifecycleCallbacks(ActivityLifecycleListener())
    }

    fun login(userId: String, metadata: Metadata) {
        Logger.log(LogLevel.DEBUG, "login -> $userId, $metadata")
        localStateHolder.login(userId, metadata)
    }

    fun logout() {
        Logger.log(LogLevel.DEBUG, "logout")
        localStateHolder.logout()
    }

    fun onEvent(eventName: String) {
        Logger.log(LogLevel.DEBUG, "onEvent -> $eventName")
        localStateHolder.addNewEvent(eventName)
        // check if we have a survey for this event
        // check if conditions are met
    }

    fun showConditionalSurvey() {
        println("Feeba showConditionalSurvey")
    }
}

data class Metadata(
    val email: String,
    val phoneNumber: String,
)