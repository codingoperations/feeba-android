package io.least.core

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object ServiceLocator {


    private val json = Json { ignoreUnknownKeys = true }


}