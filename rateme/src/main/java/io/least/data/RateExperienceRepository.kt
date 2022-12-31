package io.least.data

import android.util.Log
import android.util.LogPrinter
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

    suspend fun tagSelected(tagUpdate: TagUpdate): TagUpdate? {
        return try {
            httpClient.tagSelected(tagUpdate, serverConfig.langCode).body()
        } catch (t: Throwable) {
            Log.e(TAG, Log.getStackTraceString(t))
            null
        }
    }
}