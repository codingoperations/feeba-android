package io.feeba.data

import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class RestClient(private val requestUrl: String = "https://dev-api.feeba.io/survey-plans") {

    suspend fun getSurveyPlans(state: LocalState): FeebaResponse? {
        return try {
            val response = sendPostRequest(requestUrl, Json.encodeToString(state.userData))
            Json.decodeFromString<FeebaResponse>(response)
        } catch (t: Throwable) {
            Logger.log(LogLevel.WARN, "getSurveyPlans failed: $t")
//            null
            FeebaResponse(
                surveyPlans = listOf(
                    SurveyPlan(
                        id = "1",
                        surveyPresentation = SurveyPresentation(
                            contentHtml = "String",
                            useHeightMargin = false,
                            useWidthMargin = false,
                            isFullBleed = false,
                            displayLocation = Position.CENTER_MODAL,
                            displayDuration = 10.toDouble(),
                            pageHeight = 0,
                        ),
                        triggers = listOf(
                            listOf(
                                Trigger(
                                    property = "String",
                                    operator = "String",
                                    value = "String"
                                )
                            ),
                        ),
                    )
                ),
                sdkConfig = SdkConfig(
                    refreshIntervalSec = 60 * 1,
                ),
            )
        }
    }


    private fun sendPostRequest(urlString: String, jsonInputString: String): String {
        // Create a URL object from the provided URL string
        val url = URL(urlString)

        // Open a connection to the URL
        val connection = url.openConnection() as HttpURLConnection

        // Set the request method to POST
        connection.requestMethod = "POST"

        // Set request headers (if needed)
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer YourAccessToken")

        // Enable input/output streams
        connection.doOutput = true
        connection.doInput = true

        var resultString: String = ""
        try {
            // Write the JSON payload to the output stream
            val outputStream = DataOutputStream(connection.outputStream)
            outputStream.write(jsonInputString.toByteArray(Charsets.UTF_8))
            outputStream.flush()
            outputStream.close()

            // Get the response code
            val responseCode = connection.responseCode

            // Check if the request was successful (HTTP status code 200)
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response from the input stream
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                resultString = response.toString()
            } else {
                // Handle error cases here (e.g., by throwing an exception or logging)
                throw Exception("POST request failed with response code: $responseCode")
            }
        } finally {
            // Disconnect the connection
            connection.disconnect()
        }
        return resultString
    }
}