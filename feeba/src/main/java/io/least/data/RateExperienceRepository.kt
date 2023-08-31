package io.least.data

import android.app.Application
import android.util.Log
import io.feeba.BuildConfig
import io.least.core.ServerConfig
import io.least.core.collector.DeviceDataCollector
import io.least.core.readLocalFile
import io.least.core.writeToLocalFile
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class RateExperienceRepository(
    private val application: Application,
    private val httpClient: HttpClient,
    private val dataCollector: DeviceDataCollector,
    private var serverConfig: ServerConfig,
) {

    private val TAG = this.javaClass.simpleName
    private val jsonInstance = Json { ignoreUnknownKeys = true }

    /**
     * Check the followings
     * 1. timeout exception
     * 2. Server non 200
     * 3. file empty
     */
    suspend fun fetchRateExperienceConfig(): RateExperienceConfig {
        val response = httpClient.fetchRateExperienceConfig(serverConfig.langCode)
        Log.d(TAG, " ----------------- HTTP response: ${response.data}")
        val config = response.data?.let {
            // if parsing was successful, update the local cache in a fire and forget mode
            // We want to use a full URL with query params as a cache key. It will help us to have isolated caches for different configurations
            writeToLocalFile(response.data, application, response.path)
            Log.d(TAG, "decodeFromString executing")
            jsonInstance.decodeFromString<RateExperienceConfig>(response.data)
        } ?: run {
            Log.d(TAG, " ----------------- Reading from local cache")
            // Read from local cache
            val cachedValue = readLocalFile(application, response.path)
            jsonInstance.decodeFromString(cachedValue)
        }
        return config
    }

    suspend fun publishRateResults(result: RateExperienceResult) {
        result.commonContext = dataCollector.collect(BuildConfig.VERSION_NAME)
        // Fire and forget mode. release the coroutine as soon as possible
        kotlin.runCatching { httpClient.publishResult(result, serverConfig.langCode) }
            .onFailure { Log.w(TAG, "Error publishing results. This feedback will be ignored") }
    }

    suspend fun fetchTags(rate: Int): TagUpdate {
        val response = httpClient.fetchTags(serverConfig.langCode, rate)
        val tags = response.data?.let {
            // if parsing was successful, update the local cache in a fire and forget mode
            // We want to use a full URL with query params as a cache key. It will help us to have isolated caches for different configurations
            writeToLocalFile(response.data, application, response.path)
            jsonInstance.decodeFromString<TagUpdate>(response.data)
        } ?: run {
            Log.d(TAG, " ----------------- Reading from local cache")
            // Read from local cache
            val cachedValue = readLocalFile(application, response.path)
            jsonInstance.decodeFromString<TagUpdate>(cachedValue)
        }
        return tags
    }
}