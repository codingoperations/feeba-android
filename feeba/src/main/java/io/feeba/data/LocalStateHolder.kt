package io.feeba.data

import android.app.Application
import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger
import io.least.core.readLocalFile
import io.least.core.writeToLocalFile
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalStateHolder(private val app: Application) {
    @Volatile var lastKnownFeebaConfig: FeebaResponse? = null
    private val lastKnownLocalState: LocalState? = null
    private val surveyConfigFileName = "survey_onfig.json"
    private val localStateFileName = "local_state.json"
    private val eventCountMap = mutableMapOf<String, Int>()

    fun setFeebaConfig(response: String) {
        Logger.log(LogLevel.DEBUG, "LocalStateHolder:: Storing response: $response")
        // Update local reference
        this.lastKnownFeebaConfig = Json.decodeFromString(response)
        // Write to local file
        try {
            writeToLocalFile(response, app.applicationContext, surveyConfigFileName)
        } catch (t: Throwable) {
            Logger.log(LogLevel.WARN, "LocalStateHolder:: Failed to write local config. Error: $t")
        }
    }

    fun readLocalState(): LocalState {
        return lastKnownLocalState ?: run {
            try {
                val localValue = readLocalFile(app.applicationContext, localStateFileName)
                if (localValue.isEmpty()) {
                    Logger.log(LogLevel.WARN, "No locally cached STATE is found. Falling back to default state. Is it the first run of the app?")
                    return Defaults.localState
                }
                return Json.decodeFromString(localValue)
            } catch (t: Throwable) {
                Logger.log(LogLevel.WARN, "Failed to read local config. Falling back to default state. Error: $t")
                return Defaults.localState
            }
        }
    }

    fun readLocalConfig(): FeebaResponse? {
        return lastKnownFeebaConfig ?: run {
            try {
                val localValue = readLocalFile(app.applicationContext, surveyConfigFileName)
                if (localValue.isEmpty()) {
                    Logger.log(LogLevel.WARN, "No locally cached config is found. Is it the first run of the app?")
                    return null
                }
                return Json.decodeFromString(localValue)
            } catch (t: Throwable) {
                Logger.log(LogLevel.WARN, "Failed to read local config. Error: $t")
                return null
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
        // TODO("Not yet implemented")
        eventCountMap[eventName] = (eventCountMap[eventName] ?: 0) + 1
    }
}