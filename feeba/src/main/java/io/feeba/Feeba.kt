package io.feeba

import android.app.Activity
import android.app.Application
import android.os.Bundle
import io.least.core.ServerConfig

object Feeba {
    private var privateConfig: FeebaConfig? = null
    val config get() = privateConfig!!

    fun init(app: Application, config: FeebaConfig = defaultConfig) {
        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            var activityCount = 0
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activityCount == 0) {
                    // TODO app open count
                    // Check if it is logged in
                }
                activityCount++
            }

            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityResumed(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) {
                activityCount--
            }
        })
        println("Feeba started")
    }

    fun login(userId: String, metadata: Metadata) {
        println("Feeba login")
    }

    fun logout() {
        println("Feeba logout")
    }

    fun showConditionalSurvey() {
        println("Feeba showConditionalSurvey")
    }
}

data class FeebaConfig(
    val loggingOn: Boolean,
    val feebaId: String,
    val serviceConfig: ServerConfig
)

val defaultConfig = FeebaConfig (
    loggingOn = true,
    feebaId = "123",
    serviceConfig = ServerConfig(
        "https://dev-api.feeba.io",
        "en",
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NTUzMjA0NzYsInBheWxvYWQiOnsidXNlcklkIjoiNjM3OWEzY2ZiODMyZDE3ZmI2YmE1NTI4IiwicHJvamVjdE5hbWUiOiJBYmMifSwiaWF0IjoxNjY4OTIwNDc2fQ.RkiBEWqXTn9ozSIKDEK3PiUQP5SHwRGjJYnVErkyZdk" // for tests
    )
)
data class Metadata (
    val email: String,
    val phoneNumber: String,
)