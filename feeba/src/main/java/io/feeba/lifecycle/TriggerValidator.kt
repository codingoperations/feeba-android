package io.feeba.lifecycle

import android.app.Activity
import io.feeba.data.LocalStateHolder
import io.feeba.data.SurveyPresentation

class TriggerValidator {


    fun onEvent(eventName: String, value: String? = null, localStateHolder: LocalStateHolder): SurveyPresentation? {
        Logger.log(LogLevel.DEBUG, "onEvent -> $eventName, value: $value")
        // check if we have a survey for this event
        localStateHolder.readLocalConfig()?.let {
            // check if conditions are met
            return it.surveyPlans.first().surveyPresentation
        } ?: run {
            Logger.log(LogLevel.DEBUG, "No survey config found. Ignoring event.")
            return null
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
}