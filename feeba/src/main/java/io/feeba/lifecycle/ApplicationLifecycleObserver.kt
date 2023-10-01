package io.feeba.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.feeba.data.FeebaResponse
import io.feeba.data.LocalStateHolder
import io.feeba.data.RestClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ApplicationLifecycleObserver(
    private val localStateHolder: LocalStateHolder
) : DefaultLifecycleObserver {
    private val restClient = RestClient()
    private var configUpdateJob: Job? = null
    var appVisibility = AppVisibility.Backgrounded
    override fun onStart(owner: LifecycleOwner) { // app moved to foreground
        Logger.log(LogLevel.DEBUG, "Application moved to foreground")
        // Update the app visibility
        appVisibility = AppVisibility.Foregrounded
        configUpdateJob = GlobalScope.launch {
            val localState = localStateHolder.readLocalState()
            val updated: FeebaResponse? = restClient.getSurveyPlans(localState)
            updated?.let {
                localStateHolder.setFeebaConfig(it)
                // TODO Refresh the schedule of triggers
            }
        }
        // TODO
        // Update a count of app launches
        // Check if there is a Survey to launch
        // If there is, plan an execution when there will be an Activity in the foreground and it is not a deep linking
    }

    override fun onStop(owner: LifecycleOwner) { // app moved to background
        Logger.log(LogLevel.DEBUG, "Application moved to background")
        // Update the app visibility
        appVisibility = AppVisibility.Backgrounded

        // TODO
        // Cancel all the pending executions
        // Update last session duration
        //
    }
}