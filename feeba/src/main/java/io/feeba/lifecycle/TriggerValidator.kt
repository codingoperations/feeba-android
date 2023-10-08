package io.feeba.lifecycle

import android.app.Activity
import android.app.FragmentManager
import android.os.Bundle
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import io.feeba.survey.KEY_SURVEY_URL
import io.feeba.survey.SurveyFragment
import io.feeba.survey.showSurveyFragment

class TriggerValidator {


    fun onEvent(eventName: String, value: String? = null) {
        Logger.log(LogLevel.DEBUG, "onEvent -> $eventName, value: $value")
//        localStateHolder.onEvent(eventName)
        // check if we have a survey for this event
        // check if conditions are met
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