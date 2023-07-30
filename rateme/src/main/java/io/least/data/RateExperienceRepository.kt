package io.least.data

import android.util.Log
import io.least.core.ServerConfig
import io.least.core.collector.DeviceDataCollector
import io.least.core.readLocalFile
import io.least.core.writeToLocalFile
import io.least.rate.BuildConfig
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class RateExperienceRepository(
    private val httpClient: HttpClient,
    private val dataCollector: DeviceDataCollector,
    private var serverConfig: ServerConfig,
) {

    private val TAG = this.javaClass.simpleName

    /**
     * Check the followings
     * 1. timeout exception
     * 2. Server non 200
     * 3. file empty
     */
    suspend fun fetchRateExperienceConfig(): RateExperienceConfig {
        val response = httpClient.fetchRateExperienceConfig(serverConfig.langCode)
        val config = response.data?.let {
            // if parsing was successful, update the local cache in a fire and forget mode
            // We want to use a full URL with query params as a cache key. It will help us to have isolated caches for different configurations
            writeToLocalFile(response.data, response.path)
            Json.decodeFromString<RateExperienceConfig>(response.data)
        } ?: run {
            Log.d(TAG, " ----------------- Reading from local cache")
            // Read from local cache
            Json.decodeFromString(readLocalFile(response.path))
        }
        return config
    }

    suspend fun publishRateResults(result: RateExperienceResult) {
            result.commonContext = dataCollector.collect(BuildConfig.VERSION_NAME)
            // Fire and forget mode. release the coroutine as soon as possible
            httpClient.publishResult(result, serverConfig.langCode)
    }

    suspend fun fetchTags(rate: Int): TagUpdate {
        val response = httpClient.fetchTags(serverConfig.langCode, rate)
        val tags = response.data?.let {
            // if parsing was successful, update the local cache in a fire and forget mode
            // We want to use a full URL with query params as a cache key. It will help us to have isolated caches for different configurations
            writeToLocalFile(response.data, response.path)
            Json.decodeFromString<TagUpdate>(response.data)
        } ?: run {
            Log.d(TAG, " ----------------- Reading from local cache")
            // Read from local cache
            Json.decodeFromString<TagUpdate>(response.path)
        }
        return tags
    }
}