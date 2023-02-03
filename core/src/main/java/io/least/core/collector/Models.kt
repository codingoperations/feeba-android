package io.least.core.collector

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CommonContext(
    val platform: String,
    val version: String,
    val osName: String,
    val osVersion: String,
    val deviceModelName: String,
)

@Serializable
data class UserSpecificContext(
    val userIdentifier: String,
    val custom: JsonElement? = null
)