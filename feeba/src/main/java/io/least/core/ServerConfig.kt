package io.least.core

data class ServerConfig(
    val langCode: String,
    val apiToken: String,
    val hostUrl: String? = null,
) : java.io.Serializable