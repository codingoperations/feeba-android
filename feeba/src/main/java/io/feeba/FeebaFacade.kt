package io.feeba

import io.feeba.data.FeebaConfig
import io.feeba.data.FeebaResponse
import io.feeba.data.LocalStateHolder
import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger
import io.feeba.lifecycle.TriggerValidator
import io.least.core.ServerConfig

object FeebaFacade {

    lateinit var config: FeebaConfig
    lateinit var localStateHolder: LocalStateHolder
    private lateinit var stateManager: StateManager

    private lateinit var triggerValidator: TriggerValidator

    fun init(serverConfig: ServerConfig, localStateHolder: LocalStateHolder,  stateManager: StateManager) {
        Logger.log(LogLevel.DEBUG, "Initialization....")
        this.stateManager = stateManager
        this.localStateHolder = localStateHolder
        triggerValidator = TriggerValidator()
        config = FeebaConfig(serverConfig)
    }

    fun triggerEvent(eventName: String, value: String? = null) {
        Logger.log(LogLevel.DEBUG, "onEvent -> $eventName, value: $value")
        localStateHolder.onEvent(eventName)
        // check if we have a survey for this event
        val validatorResult = triggerValidator.onEvent(eventName, value, localStateHolder)
        validatorResult?.let {
            stateManager.showEventSurvey(it.surveyPresentation, it.ruleSet, eventName)
        }
    }

    fun pageOpened(pageName: String) {
        Logger.log(LogLevel.DEBUG, "pageOpened -> $pageName")
        localStateHolder.pageOpened(pageName)
        // check if we have a survey for this event
        val validationResult = triggerValidator.pageOpened(pageName, localStateHolder)
        validationResult?.let {
            stateManager.showPageSurvey(it.surveyPresentation, it.ruleSet, pageName)
        }
    }

    fun pageClosed(pageName: String) {
        Logger.log(LogLevel.DEBUG, "pageClosed -> $pageName")
        localStateHolder.pageClosed(pageName)
        // check if we have a survey for this event
        stateManager.pageClosed(pageName)
    }

    fun showConditionalSurvey() {
        println("Feeba showConditionalSurvey")
    }

    fun feebaResponse(): FeebaResponse? {
        return localStateHolder.lastKnownFeebaConfig
    }

    object User {
        fun login(userId: String, email: String? = null, phoneNumber: String? = null) {
            Logger.log(LogLevel.DEBUG, "login -> $userId, $email, $phoneNumber")
            localStateHolder.login(userId, email, phoneNumber)
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
            localStateHolder.addTags(tags = tags)
        }
    }
}
