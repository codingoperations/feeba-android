package io.feeba

import android.os.Handler
import android.os.Looper
import io.feeba.data.LocalStateHolder
import io.feeba.data.RestClient
import io.feeba.data.RuleSet
import io.feeba.data.SurveyPresentation
import io.feeba.lifecycle.AppLifecycleListener
import io.feeba.lifecycle.AppState
import io.feeba.lifecycle.GenericAppLifecycle
import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger
import io.feeba.lifecycle.OnWindowLifecycleListener
import io.feeba.lifecycle.WindowState
import io.feeba.survey.SurveyViewController
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class StateManager(private val lifecycle: GenericAppLifecycle, private val localStateHolder: LocalStateHolder) {
    private var surveyController: SurveyViewController? = null
    private val delayedTasks = mutableMapOf<String, Runnable>()
    private val handler = Handler(Looper.getMainLooper())
    private val restClient = RestClient()

    // State
    private var appVisibility = AppVisibility.Backgrounded

    init {
        lifecycle.windowLifecycleListener = object : OnWindowLifecycleListener {
            override fun onWindow(state: WindowState) {
                Logger.log(LogLevel.DEBUG, "Window moved to $state")
                when (state) {
                    WindowState.CREATED -> {
//                        TODO()
                    }

                    WindowState.OPENED -> {
//                        TODO()
                    }

                    WindowState.CLOSED -> {
//                        TODO()
                    }

                    WindowState.DESTROYED -> {
//                        TODO()
                    }
                }
            }
        }

        lifecycle.appLifecycleListener = object : AppLifecycleListener {
            override fun onLifecycleEvent(state: AppState) {
                Logger.log(LogLevel.DEBUG, "Application moved to $state")
                when (state) {
                    AppState.CREATED -> {
                        Logger.log(LogLevel.DEBUG, "StateManager::AppLifecycleEvent -> CREATED")
                        GlobalScope.launch {
                            val localState = localStateHolder.readAppHistoryState()
                            val updated: String? = restClient.getSurveyPlans(localState)
                            updated?.let {
                                localStateHolder.setFeebaConfig(it)
                            }
                        }
                        appVisibility = AppVisibility.Foregrounded
                    }

                    AppState.FOREGROUND -> {
                        appVisibility = AppVisibility.Foregrounded
                    }

                    AppState.BACKGROUND -> {
                        appVisibility = AppVisibility.Backgrounded
                    }
                }
            }
        }
    }

    fun showSurvey(presentation: SurveyPresentation, ruleSet: RuleSet, associatedKey: String) {
        Logger.log(LogLevel.DEBUG, "StateManager::showSurvey")
        val surveyDelay = ruleSet.getSurveyDelaySec()
        if (surveyDelay > 0) {
            Logger.log(LogLevel.DEBUG, "StateManager::showSurvey - Scheduling survey for $surveyDelay Sec")
            val runnable = Runnable {
                Logger.log(LogLevel.DEBUG, "StateManager::showSurvey - Executing scheduled survey")
                initializeSurveyViewController(presentation, ruleSet)
            }
            delayedTasks[associatedKey] = runnable
            handler.postDelayed(runnable, surveyDelay * 1000)
        } else {
            Logger.log(LogLevel.DEBUG, "StateManager::showSurvey - Showing survey immediately")
            initializeSurveyViewController(presentation, ruleSet)
        }
    }

    private fun initializeSurveyViewController(presentation: SurveyPresentation, ruleSet: RuleSet) {
        if (appVisibility == AppVisibility.Backgrounded) {
            Logger.log(LogLevel.WARN, "StateManager::showSurvey - App is in background, skipping")
            return
        }
        SurveyViewController(presentation, ruleSet, localStateHolder.readAppHistoryState(), object : SurveyViewController.SurveyViewLifecycleListener {
            override fun onSurveyWasShown() {
                Logger.log(LogLevel.DEBUG, "SurveyViewController::onSurveyWasShown")
            }

            override fun onSurveyWasDismissed() {
                Logger.log(LogLevel.DEBUG, "SurveyViewController::onSurveyWasDismissed")
                surveyController = null
            }

        }).also {
            surveyController = it
            it.start(lifecycle)
        }
    }

    fun pageClosed(pageName: String) {
        Logger.log(LogLevel.DEBUG, "StateManager::pageClosed -> $pageName")
        cancelPendingSurveys(pageName)
        surveyController?.destroy(false)
        surveyController = null
    }

    fun cancelEventRelatedSurveys(eventName: String) {
        Logger.log(LogLevel.DEBUG, "StateManager::cancelEventRelatedSurveys -> $eventName")
    }

    private fun cancelPendingSurveys(pageName: String) {
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