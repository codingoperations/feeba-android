package io.feeba.data

import io.feeba.data.state.AppHistoryState
import io.feeba.data.state.Defaults
import io.feeba.data.state.StateStorageInterface
import io.feeba.data.state.UserData
import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class LocalStateHolder(private val stateStorage: StateStorageInterface) {
    @Volatile
    var lastKnownFeebaConfig: FeebaResponse? = null
    private val lastKnownAppHistoryState: AppHistoryState? = null

    private val eventCountMap = mutableMapOf<String, Int>()
    private val jsonInstance = Json { ignoreUnknownKeys = true }

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

    fun login(userData: UserData) {
        // TODO("Not yet implemented")
    }

    fun logout() {
        // TODO("Not yet implemented")
    }

    fun onEvent(eventName: String) {
        eventCountMap[eventName] = (eventCountMap[eventName] ?: 0) + 1
    }

    fun pageOpened(pageName: String) {
//        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }
}