package io.feeba.survey

import android.content.Context
import android.webkit.JavascriptInterface
import io.feeba.data.state.AppHistoryState
import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger

enum class CallToAction(val value: String) {
    CLOSE_SURVEY("closeSurvey");

    companion object {
        fun safeValueOf(value: String) = values().find { it.value == value } ?: CLOSE_SURVEY
    }
}

class JsInterface(private val mContext: Context, private val appHistoryState: AppHistoryState, private val onSurveyEndCallback: (cta: CallToAction) -> Unit)  {

    init {
        Logger.log(LogLevel.DEBUG, "JsInterface::init, appHistoryState=$appHistoryState")
    }
    /** Show a toast from the web page  */
    @JavascriptInterface
    fun endOfSurvey(callToAction: String) {
        val cta: CallToAction = CallToAction.safeValueOf(callToAction)
        onSurveyEndCallback(cta)
    }

    @JavascriptInterface
    fun getCurrentState(): AppHistoryState {
        Logger.log(LogLevel.DEBUG, "JsInterface::getCurrentState, appHistoryState=$appHistoryState")
        return appHistoryState
    }
}