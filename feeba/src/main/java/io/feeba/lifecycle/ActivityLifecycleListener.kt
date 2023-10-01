package io.feeba.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * The following code is based on @see <a href="https://developer.android.com/guide/components/activities/activity-lifecycle#alc">Activity Lifecycle</a>.
 */
internal class ActivityLifecycleListener() : Application.ActivityLifecycleCallbacks {


    private var activityCount = 0
    private var curActivity: Activity? = null
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Logger.log(LogLevel.DEBUG, "onActivityCreated: $activity")
        activityCount++
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
}

enum class AppVisibility{
    Backgrounded, Foregrounded
}