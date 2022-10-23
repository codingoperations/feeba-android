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


    suspend fun fetchRateExperienceConfig(): RateExperienceConfig {
        // TODO Handle Http call errors
        val response = httpClient.fetchRateExperienceConfig(serverConfig.langCode).body()
        if (response == null) {
            Log.e(this.javaClass.simpleName, "Server returned an empty response")
            // TODO how can we fail safe if the server's response is an empty?
            throw NullPointerException()
        }
        return response
    }

    suspend fun publishRateResults(result: RateExperienceResult) {
        result.commonContext = dataCollector.collect(BuildConfig.VERSION_NAME)
        httpClient.publishResult(result, serverConfig.langCode)
    }
}