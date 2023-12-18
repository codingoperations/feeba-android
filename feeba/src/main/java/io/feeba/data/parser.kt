package io.feeba.data

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

fun decodeFeebaResponse(json: String): FeebaResponse {
    return Json.decodeFromString<FeebaResponse>(json)
}

fun isEvent(triggerCondition: TriggerCondition): Boolean {
    return triggerCondition.type == RuleType.EVENT
}

fun isPageTrigger(ruleSet: RuleSet) : Boolean {
    for (triggerCondition in ruleSet.triggers) {
        if (triggerCondition.type == RuleType.SCREEN) {
            return true
        }
    }
    return false
}