package io.feeba

import android.app.Application
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import io.feeba.data.FeebaConfig
import io.feeba.data.LocalStateHolder
import io.feeba.data.sql.AndroidStateStorage
import io.feeba.data.state.UserData
import io.feeba.lifecycle.ActivityLifecycleListener
import io.feeba.lifecycle.ApplicationLifecycleObserver
import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger
import io.feeba.lifecycle.TriggerValidator
import io.least.core.ServerConfig

object Feeba {
    lateinit var app: Application
    val appContext: Context
        get() = app.applicationContext

    lateinit var config: FeebaConfig
    private lateinit var localStateHolder: LocalStateHolder
    private lateinit var triggerValidator: TriggerValidator
    private lateinit var activityListener: ActivityLifecycleListener
    private var isInitialized = false

    fun init(app: Application, serverConfig: ServerConfig) {
        if (isInitialized) {
            Logger.log(LogLevel.WARN, "Feeba already initialized. Ignoring init call.")
            return
        }
        Logger.log(LogLevel.DEBUG, "Initialization....")
        localStateHolder = LocalStateHolder(AndroidStateStorage(app.applicationContext))
        triggerValidator = TriggerValidator()
        this.app = app
        config = FeebaConfig(serverConfig)
        ProcessLifecycleOwner.get().lifecycle.addObserver(ApplicationLifecycleObserver(localStateHolder))
        app.registerActivityLifecycleCallbacks(ActivityLifecycleListener().also { activityListener = it })
    }

    fun triggerEvent(eventName: String, value: String? = null) {
        Logger.log(LogLevel.DEBUG, "onEvent -> $eventName, value: $value")
        localStateHolder.onEvent(eventName)
        // check if we have a survey for this event
        val surveyPresentation = triggerValidator.onEvent(eventName, value, localStateHolder)
        surveyPresentation?.let {
            activityListener.startSurveyRendering(it)
        }
    }

    fun pageOpened(pageName: String) {
        Logger.log(LogLevel.DEBUG, "pageOpened -> $pageName")
        localStateHolder.pageOpened(pageName)
        // check if we have a survey for this event
        val validationResult = triggerValidator.pageOpened(pageName, localStateHolder)
        validationResult?.let {
            activityListener.showSurveyWithDelay(pageName, it.first, it.second)
        }
    }

    fun pageClosed(pageName: String) {
        Logger.log(LogLevel.DEBUG, "pageClosed -> $pageName")
        localStateHolder.pageClosed(pageName)
        // check if we have a survey for this event
        activityListener.cancelPendingSurveys(pageName)
    }

    fun showConditionalSurvey() {
        println("Feeba showConditionalSurvey")
    }

    val User = object {
        fun login(userId: String, email: String? = null, phoneNumber: String? = null) {
            Logger.log(LogLevel.DEBUG, "login -> $userId, $email, $phoneNumber")
            localStateHolder.login(UserData(userId, email, phoneNumber))
        }

        fun logout() {
            Logger.log(LogLevel.DEBUG, "logout")
            localStateHolder.logout()
        }

        fun addPhoneNumber(phoneNumber: String) {
            Logger.log(LogLevel.DEBUG, "addPhoneNumber -> $phoneNumber")
            localStateHolder.updateUserData(phoneNumber = phoneNumber)
        }

        fun addEmail(email: String) {
            Logger.log(LogLevel.DEBUG, "addEmail -> $email")
            localStateHolder.updateUserData(email = email)
        }

        /**
         * Set the language of the user. This will be used to filter surveys.
         * language: ISO 639-1 code
         */
        fun setLanguage(language: String) {
            Logger.log(LogLevel.DEBUG, "setLanguage -> $language")
            if (language.length != 2) {
                Logger.log(LogLevel.ERROR, "This function expects a ISO 639-1 code. e.g. 'en' for English. Ignoring the call.")
                return
            }
            localStateHolder.updateUserData(language = language)
        }

        fun addTag(tags: Map<String, String>) {
            Logger.log(LogLevel.DEBUG, "addTag -> $tags")
            localStateHolder.updateUserData(tags = tags)
        }
    }
}
