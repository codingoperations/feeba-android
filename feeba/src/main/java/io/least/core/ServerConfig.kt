package io.least.core

data class ServerConfig(
    val hostUrl: String,
    val langCode: String,
    val apiToken: String,
) : java.io.Serializable