package io.least.data

import android.util.Log
import io.least.core.ServerConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

class HttpClient(private val serverConfig: ServerConfig) {

    private val logging = HttpLoggingInterceptor().apply { this.setLevel(HttpLoggingInterceptor.Level.BODY) }
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .callTimeout(5, TimeUnit.SECONDS).build()

    private val JSON_MIME = "application/json"
    private val requestBuilder = Request.Builder()
        .header("x-rate-exp", serverConfig.apiToken)
        .addHeader("Accept", JSON_MIME)
        .addHeader("Content-Type", JSON_MIME)


    suspend fun fetchRateExperienceConfig(langCode: String): Response {
        val request = requestBuilder.url("${serverConfig.hostUrl}/v1/rating/sdk/config?lang=$langCode").build()
        return sendHttpRequest(request)
    }

    suspend fun publishResult(result: RateExperienceResult, langCode: String): Response {
        val request = requestBuilder.url("${serverConfig.hostUrl}/v1/rating/sdk/user_rating?lang=$langCode")
            .post(Json.encodeToString(result).toRequestBody(JSON_MIME.toMediaType())).build()
        return sendHttpRequest(request)
    }

    suspend fun fetchTags(langCode: String, rate: Int): Response {
        val request = requestBuilder.url("${serverConfig.hostUrl}/v1/rating/sdk/tags?lang=${langCode}&rate=${rate}").build()
        return sendHttpRequest(request)
    }

    private fun sendHttpRequest(request: Request): Response {
        try {
            val response: String = okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("fetchRateExpConfig", response.toString())
                    throw IOException("Unsuccessful response $response")
                }
                response.body?.let { return@use it.string() }
                throw IOException("Server returned an empty response")
            }
            return Response(request.url.toString(), data = response, null)
        } catch (t: Throwable) {
            return Response(request.url.toString(), data = null, throwable = t)
        }
    }
}

data class Response(
    val path: String,
    val data: String?,
    val throwable: Throwable?,
)