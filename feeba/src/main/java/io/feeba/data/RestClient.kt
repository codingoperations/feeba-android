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

    suspend fun getSurveyPlans(state: LocalState): String? {
        return try {
            val response = sendPostRequest(requestUrl, Json.encodeToString(state.userData))
            if (response == "") {
               return null
            }
            return response
        } catch (t: Throwable) {
            Logger.log(LogLevel.WARN, "getSurveyPlans failed: $t")
//            null
            val result  = """
                {
                    "surveyPlans": [
                        {
                            "id": "1",
                            "surveyPresentation": {
                                "surveyWebAppUrl": "https://dev-dashboard.feeba.io/s/feeba/64f2e4a38c4282406ad01315",
                                "useHeightMargin": false,
                                "useWidthMargin": false,
                                "isFullBleed": false,
                                "displayLocation": "CENTER_MODAL",
                                "displayDuration": 10.0,
                                "maxWidgetHeightInPercent": 70,
                                "maxWidgetWidthInPercent": 90
                            },
                            "triggerConditions": [
                                [
                                    {
                                        "property": "on_ride_end",
                                        "operator": "ex",
                                        "value": ""
                                    }
                                ]
                            ]
                        }
                    ],
                    "sdkConfig": {
                        "refreshIntervalSec": 60
                    }
                }
            """.trimIndent()
            Logger.log(LogLevel.WARN, "SCHEMA: ${Json.encodeToString(result)}")
            result
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