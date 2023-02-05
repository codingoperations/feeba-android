package io.least

import io.least.core.ServerConfig
import io.least.core.ServiceLocator
import io.least.data.HttpClient

object ServiceLocator {

    fun getHttpClient(config: ServerConfig): HttpClient {
        return ServiceLocator.retrofitInstance(config).create(HttpClient::class.java)
    }
}