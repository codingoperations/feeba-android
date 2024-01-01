package io.feeba.data

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