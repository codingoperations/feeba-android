package io.least.core

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit


object ServiceLocator {
    private val logging =
        HttpLoggingInterceptor().apply { this.setLevel(HttpLoggingInterceptor.Level.BODY) }
    private val clientBuilder = OkHttpClient.Builder().addInterceptor(logging)


    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalSerializationApi::class)
    fun retrofitInstance(config: ServerConfig): Retrofit {
        return Retrofit.Builder()
            .baseUrl(config.hostUrl)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(clientBuilder.addNetworkInterceptor {
                val requestBuilder = it.request().newBuilder()
                requestBuilder.header("x-rate-exp", config.apiToken)
                it.proceed(requestBuilder.build());
            }.build())
            .build()
    }
}