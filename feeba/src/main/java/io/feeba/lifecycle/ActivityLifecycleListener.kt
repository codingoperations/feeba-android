package io.feeba.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import io.feeba.data.RuleType
import io.feeba.data.SurveyPresentation
import io.feeba.data.TriggerCondition
import io.feeba.survey.SurveyViewController

/**
 * The following code is based on @see <a href="https://developer.android.com/guide/components/activities/activity-lifecycle#alc">Activity Lifecycle</a>.
 */
internal class ActivityLifecycleListener() : Application.ActivityLifecycleCallbacks {

    private var activityCount = 0
    private var curActivity: Activity? = null
    private val activityNameSet = mutableSetOf<String>()
    private var surveyViewController: SurveyViewController? = null
    private val delayedTasks = mutableMapOf<String, Runnable>()
    private val handler = Handler(Looper.getMainLooper())
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Logger.log(LogLevel.DEBUG, "onActivityCreated: $activity")
        activityCount++
        activityNameSet.add(activity::class.java.simpleName)
    }

    override fun onActivityStarted(activity: Activity) {
        Logger.log(LogLevel.DEBUG, "onActivityStarted: $activity")
    }

    override fun onActivityResumed(activity: Activity) {
        Logger.log(LogLevel.DEBUG, "onActivityResumed: $activity")
        curActivity = activity
        // TODO add this activity to the list of activities
    }

    override fun onActivityPaused(activity: Activity) {
        Logger.log(LogLevel.DEBUG, "onActivityPaused: $activity")
        if (activity === curActivity) {
            curActivity = null
        }
    }

    override fun onActivityStopped(activity: Activity) {
        Logger.log(LogLevel.DEBUG, "onActivityStopped: $activity")
        if (activity === curActivity) {
            curActivity = null
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        Logger.log(LogLevel.DEBUG, "onActivityDestroyed: $activity")
        activityCount--
        if (activity === curActivity) {
            curActivity = null
        }
        activityCount--
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    fun showSurveyDialogOnCurrentActivity(presentation: SurveyPresentation) {
        Logger.log(LogLevel.DEBUG, "ActivityLifecycleListener::showSurveyDialogOnCurrentActivity")
        curActivity?.let { activity ->
            if (surveyViewController != null) return
            surveyViewController = SurveyViewController(presentation, object : SurveyViewController.SurveyViewLifecycleListener {
                override fun onSurveyWasShown() {
                    Logger.log(LogLevel.DEBUG, "SurveyViewController::onSurveyWasShown")
                }

                override fun onSurveyWillDismiss() {

                }

                override fun onSurveyWasDismissed() {
                    Logger.log(LogLevel.DEBUG, "SurveyViewController::onSurveyWasDismissed")
                    surveyViewController?.removeAllViews()
                    surveyViewController = null
                }

            }).also {
                it.showSurvey(activity)
            }
        }
    }

    fun showSurveyWithDelay(pageName: String, presentation: SurveyPresentation, delay: Long) {
        Logger.log(LogLevel.DEBUG, "ActivityLifecycleListener::showSurveyWithDelay")
        val task = Runnable {
            showSurveyDialogOnCurrentActivity(presentation)
        }

        delayedTasks[pageName] = task
        handler.postDelayed(task, delay)
    }

    fun cancelPendingSurveys(pageName: String) {
        Logger.log(LogLevel.DEBUG, "ActivityLifecycleListener::cancelPendingSurveys")
        delayedTasks.remove(pageName)?.let {
            Logger.log(LogLevel.DEBUG, "---> Removing ")
            handler.removeCallbacks(it)
        }
    }
}

enum class AppVisibility {
    Backgrounded, Foregrounded
}