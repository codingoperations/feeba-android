package io.least.data

import android.util.Log
import io.least.core.ServerConfig
import io.least.core.collector.DeviceDataCollector
import io.least.rate.BuildConfig

class RateExperienceRepository(
    private val httpClient: HttpClient,
    private val dataCollector: DeviceDataCollector,
    private var serverConfig: ServerConfig,
) {

    private val TAG = this.javaClass.simpleName

    suspend fun fetchRateExperienceConfig(): RateExperienceConfig {
        // TODO Handle Http call errors
        val response = httpClient.fetchRateExperienceConfig(serverConfig.langCode).body()
        if (response == null) {
            Log.e(TAG, "Server returned an empty response")
            // TODO how can we fail safe if the server's response is an empty?
            throw NullPointerException()
        }
        return response
    }

    suspend fun publishRateResults(result: RateExperienceResult) {
        result.commonContext = dataCollector.collect(BuildConfig.VERSION_NAME)
        httpClient.publishResult(result, serverConfig.langCode)
    }

    suspend fun fetchTags(rate: Int): TagUpdate {
        return try {
            val body = httpClient.fetchTags(serverConfig.langCode, rate).body()
            body ?: throw NullPointerException("Server returned an empty response")
        } catch (t: Throwable) {
            Log.e(TAG, Log.getStackTraceString(t))
            TagUpdate(listOf())
        }
    }
}