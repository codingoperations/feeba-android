package sample.project

import io.feeba.data.FeebaResponse
import io.feeba.data.RuleType
import sample.project.events.EventTriggerUiModel

fun extractEvents(feebaResponse: FeebaResponse): List<EventTriggerUiModel> {
    return feebaResponse.surveyPlans
        .flatMap { it.ruleSetList }
        .filter { it.triggers.any { it.type == RuleType.EVENT } }
        .map { EventTriggerUiModel(
            it.triggers.first { it.type == RuleType.EVENT }.eventName, "") }
}