package io.feeba.survey

import android.app.Application
import android.util.Log
import io.feeba.BuildConfig
import io.least.core.ServerConfig
import io.least.core.collector.DeviceDataCollector
import io.least.core.readLocalFile
import io.least.core.writeToLocalFile
import io.least.data.HttpClient
import kotlinx.serialization.json.Json

class SurveyRepository(
    private val application: Application,
    private val httpClient: HttpClient,
    private val dataCollector: DeviceDataCollector,
    private var serverConfig: ServerConfig,
) {

    private val TAG = this.javaClass.simpleName
    private val jsonInstance = Json { ignoreUnknownKeys = true }

//    suspend fun fetchSurvey(surveyId: String): Survey {
//        return httpClient.fetchSurvey(surveyId)
//    }
//
//    suspend fun publishRateResults(result: SurveyResult) {
//        // FIXME must be fire and forget
////        return httpClient.publishSurveyResults()
//    }
}