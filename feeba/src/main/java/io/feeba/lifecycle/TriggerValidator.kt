package io.feeba.lifecycle

import android.app.Activity
import io.feeba.data.LocalStateHolder
import io.feeba.data.RuleType
import io.feeba.data.SurveyPresentation
import io.feeba.data.TriggerCondition
import io.feeba.data.isEvent
import io.feeba.data.isPageTrigger

class TriggerValidator {


    fun onEvent(eventName: String, value: String? = null, localStateHolder: LocalStateHolder): SurveyPresentation? {
        Logger.log(LogLevel.DEBUG, "TriggerValidator:: onEvent -> $eventName, value: $value")
        // check if we have a survey for this event
        val config = localStateHolder.readLocalConfig()
        // check if we have a survey for this event
        Logger.log(LogLevel.DEBUG, "TriggerValidator:: surveyPlans -> ${config.surveyPlans}")
        for (surveyPlan in config.surveyPlans) {
            for (andTrigger in surveyPlan.triggerConditions) {
                // if all conditions are met, return the survey
                var allConditionsMet = false
                for (triggerCondition: TriggerCondition in andTrigger) {
                    if (isEvent(triggerCondition) && triggerCondition.property == eventName) {
                        allConditionsMet = true
                    }
                }
                if (allConditionsMet) {
                    return surveyPlan.surveyPresentation
                }
            }
        }
        return null

    }

    fun pageOpened(pageName: String, localStateHolder: LocalStateHolder): Pair<SurveyPresentation, Long>? {
        Logger.log(LogLevel.DEBUG, "TriggerValidator:: pageOpened -> $pageName")
        // check if we have a survey for this event
        val config = localStateHolder.readLocalConfig()
        // check if we have a survey for this event
        Logger.log(LogLevel.DEBUG, "TriggerValidator:: surveyPlans -> ${config.surveyPlans}")
        for (surveyPlan in config.surveyPlans) {
            for (andTrigger in surveyPlan.triggerConditions) {
                // if all conditions are met, return the survey
                var allConditionsMet = false
                if (isPageTrigger(andTrigger)) {
                    allConditionsMet = true
                }
                if (allConditionsMet) {
                    if (andTrigger.none { it.property == pageName }) {
                        // page name is not in the trigger conditions, we pass the condition block
                        continue
                    }
                    val surveyOpenDelaySec = try {
                        andTrigger.filter { it.type == RuleType.SESSION_DURATION }.getOrNull(0)?.value?.toLongOrNull() ?: 0
                    } catch (throwable: Throwable) {
                        Logger.log(LogLevel.ERROR, "Failed to parse page timing condition value: $throwable")
                        0
                    }
                    return Pair(surveyPlan.surveyPresentation, surveyOpenDelaySec * 1000)
                }
            }
        }
        return null
    }
}

fun validateEvent(triggerCondition: TriggerCondition) {
    when (triggerCondition.operator) {
        "ex" -> {}
        "eq" -> {}
        "neq" -> {}
        "gt" -> {}
        "gte" -> {}
        "lt" -> {}
        "lte" -> {}
        else -> {
            Logger.log(LogLevel.WARN, "Unknown operator: ${triggerCondition.operator}")
            null
        }
    }
}

fun onActivityPaused(activity: Activity) {
    Logger.log(LogLevel.DEBUG, "onActivityPaused: $activity")
    // TODO remove any callbacks that are waiting to run on this activity
}

fun onActivityResumed(activity: Activity) {
    Logger.log(LogLevel.DEBUG, "onActivityResumed: $activity")
    // TODO Check if there is any pending survey to show
}

fun onAppOpened(starterActivity: Activity) {
    Logger.log(LogLevel.DEBUG, "onAppOpened: $starterActivity")
    // TODO Check if there is any pending survey to show
}

private fun showSurveyDialog(activity: Activity) {
    // TODO show survey dialog
//        val fm: FragmentManager = activity.fragmentManager
//        SurveyFragment()
//            .apply {
//                arguments = Bundle().apply {
//                    putString(
//                        KEY_SURVEY_URL,
//                        "http://dev-dashboard.feeba.io/s/feeba/6504ee57ba0d101292e066a8"
//                    )
//                }
//            }
//            .show(
//                fm,
//                "SurveyFragment"
//            )
//        showSurveyFragment(activity.fragmentManager, "http://dev-dashboard.feeba.io/s/feeba/6504ee57ba0d101292e066a8")
}