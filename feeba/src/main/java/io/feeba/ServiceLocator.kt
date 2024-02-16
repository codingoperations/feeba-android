package io.feeba

import kotlinx.serialization.json.Json

object ServiceLocator {
    val jsonInstance = Json {
        ignoreUnknownKeys = true
    }
}