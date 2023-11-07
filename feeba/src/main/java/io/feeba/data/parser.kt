package io.feeba.data

import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

fun decodeFeebaResponse(json: String): FeebaResponse {
    return Json.decodeFromString<FeebaResponse>(json)
}

fun isEvent(triggerCondition: TriggerCondition): Boolean {
    return triggerCondition.property != "session_duration" &&
    triggerCondition.property != "since_last"
}