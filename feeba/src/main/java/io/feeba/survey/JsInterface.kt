package io.feeba.survey

import android.content.Context
import android.webkit.JavascriptInterface
import androidx.fragment.app.DialogFragment

enum class CallToAction(val value: String) {
    CLOSE_SURVEY("closeSurvey");

    companion object {
        fun safeValueOf(value: String) = values().find { it.value == value } ?: CLOSE_SURVEY
    }
}

class JsInterface(private val mContext: Context, private val dialog: DialogFragment) {

    /** Show a toast from the web page  */
    @JavascriptInterface
    fun endOfSurvey(callToAction: String) {
        val cta: CallToAction = CallToAction.safeValueOf(callToAction)
        when (cta) {
            CallToAction.CLOSE_SURVEY -> {
                dialog.dismiss()
            }
        }
    }
}