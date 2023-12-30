package io.feeba.lifecycle

import android.app.Activity
import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner

class AndroidLifecycleManager(app: Application) : GenericAppLifecycle {
    private var activityCount = 0
    var curActivity: Activity? = null
    private val activityNameSet = mutableSetOf<String>()

    // App State
    private var appStartEventFired = false
    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onStart(owner: androidx.lifecycle.LifecycleOwner) {
                if (!appStartEventFired) {
                    appStartEventFired = true
                    appLifecycleListener?.onLifecycleEvent(AppState.CREATED)
                }
                appLifecycleListener?.onLifecycleEvent(AppState.FOREGROUND)
            }

            override fun onStop(owner: androidx.lifecycle.LifecycleOwner) {
                appLifecycleListener?.onLifecycleEvent(AppState.BACKGROUND)
            }
        })
        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: android.os.Bundle?) {
                Logger.log(LogLevel.DEBUG, "onActivityCreated: $activity")
                activityCount++
                activityNameSet.add(activity::class.java.simpleName)

                windowLifecycleListener?.onWindow(WindowState.CREATED)
            }

            override fun onActivityStarted(activity: Activity) {
                Logger.log(LogLevel.DEBUG, "onActivityStarted: $activity")

                windowLifecycleListener?.onWindow(WindowState.OPENED)
            }

            override fun onActivityResumed(activity: Activity) {
                Logger.log(LogLevel.DEBUG, "onActivityResumed: $activity")
                curActivity = activity

                windowLifecycleListener?.onWindow(WindowState.OPENED)
            }

            override fun onActivityPaused(activity: Activity) {
                Logger.log(LogLevel.DEBUG, "onActivityPaused: $activity")
                if (activity === curActivity) {
                    curActivity = null
                }
                windowLifecycleListener?.onWindow(WindowState.CLOSED)
            }

            override fun onActivityStopped(activity: Activity) {
                Logger.log(LogLevel.DEBUG, "onActivityStopped: $activity")
                if (activity === curActivity) {
                    curActivity = null
                }

                windowLifecycleListener?.onWindow(WindowState.CLOSED)
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: android.os.Bundle) {
                // no-op
            }

            override fun onActivityDestroyed(activity: Activity) {
                Logger.log(LogLevel.DEBUG, "onActivityDestroyed: $activity")
                activityCount--
                if (activity === curActivity) {
                    curActivity = null
                }
                activityCount--

                windowLifecycleListener?.onWindow(WindowState.DESTROYED)
            }
        })
    }

    override var windowLifecycleListener: OnWindowLifecycleListener? = null
    override var appLifecycleListener: AppLifecycleListener? = null
}