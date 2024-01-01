package io.feeba.data

import io.feeba.ServiceLocator
import io.feeba.data.state.AppHistoryState
import io.feeba.data.state.Defaults
import io.feeba.data.state.StateStorageInterface
import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class LocalStateHolder(private val stateStorage: StateStorageInterface) {
    @Volatile
    var lastKnownFeebaConfig: FeebaResponse? = null
    private val lastKnownAppHistoryState: AppHistoryState? = null

    private val eventCountMap = mutableMapOf<String, Int>()
    private val jsonInstance = ServiceLocator.jsonInstance

    fun setFeebaConfig(response: String) {
        Logger.log(LogLevel.DEBUG, "LocalStateHolder:: Storing response: $response")
        // Update local reference
        try {
            jsonInstance.decodeFromString<FeebaResponse>(response).also {
                this.lastKnownFeebaConfig = it
                stateStorage.feebaResponse = it
            }
        } catch (t: Throwable) {
            Logger.log(LogLevel.ERROR, "LocalStateHolder:: Failed to parse response. Error: $t")
        }
    }


    fun readLocalConfig(): FeebaResponse {
        return lastKnownFeebaConfig ?: run {
            stateStorage.feebaResponse
        }
    }


    fun readLocalState(): AppHistoryState {
        return lastKnownAppHistoryState ?: run {
            try {
                stateStorage.state
            } catch (t: Throwable) {
                Logger.log(LogLevel.WARN, "Failed to read local config. Falling back to default state. Error: $t")
                return Defaults.appHistoryState
            }
        }
    }

    fun login(userId: String, email: String?, phoneNumber: String?) {
        readLocalState().apply {
         this.userData = this.userData.copy(
                userId = userId,
                email = email ?: this.userData.email,
                phoneNumber = phoneNumber ?: this.userData.phoneNumber
            )
            stateStorage.state = this
        }
    }

    fun logout() {
        readLocalState()
        stateStorage.eraseEventAndPageLogs()
    }

    fun onEvent(eventName: String) {
        eventCountMap[eventName] = (eventCountMap[eventName] ?: 0) + 1
        stateStorage.addEventRecord(eventName, "")
    }

    fun pageOpened(pageName: String) {
        stateStorage.addPageOpenRecord(pageName, "")
    }

    fun pageClosed(pageName: String) {
//        TODO("Not yet implemented")
    }

    fun updateUserData(
        phoneNumber: String? = null,
        email: String? = null,
        language: String? = null,
        tags: Map<String, String>? = null,
    ) {
        readLocalState().apply {
            this.userData = this.userData.copy(
                phoneNumber = phoneNumber ?: this.userData.phoneNumber,
                email = email ?: this.userData.email,
                langCode = language ?: this.userData.langCode,
                tags = tags?.toMutableMap() ?: this.userData.tags
            )
            stateStorage.state = this
        }
    }

    fun addTags(tags: Map<String, String>) {
        readLocalState().apply {
            this.userData.tags.putAll(tags)
            stateStorage.state = this
        }
    }
}