package io.feeba.data

import android.app.Application
import io.feeba.Metadata
import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger
import io.least.core.readLocalFile
import io.least.core.writeToLocalFile
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalStateHolder(private val app: Application) {
    @Volatile private var lastKnownFeebaConfig: FeebaResponse? = null
    private val lastKnownLocalState: LocalState? = null
    private val surveyConfigFileName = "survey_onfig.json"
    private val localStateFileName = "local_state.json"

    fun setFeebaConfig(response: FeebaResponse) {
        // Update local reference
        this.lastKnownFeebaConfig = response
        // Write to local file
        try {
            val json = Json.encodeToString(response)
            writeToLocalFile(json, app.applicationContext, surveyConfigFileName)
        } catch (t: Throwable) {
            Logger.log(LogLevel.WARN, "Failed to write local config. Error: $t")
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

    fun login(userId: String, metadata: Metadata) {
        // TODO("Not yet implemented")
    }

    fun logout() {
        // TODO("Not yet implemented")
    }

    fun addNewEvent(eventName: String) {
        // TODO("Not yet implemented")
    }
}