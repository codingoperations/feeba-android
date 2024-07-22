package io.feeba.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import io.feeba.FeebaFacade
import io.feeba.Utils
import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger

/**
 * Requirements:
 * - The InlineSurveyView's size is not known at the start. You can read the parent's size only after the parent is laid out.
 * In ideal world, inlineSurvey would notify WebView every time when its size changes. This requires a contract change between Android and SPA
 * - MATCH_PARENT
 * -- WIDTH: ALWAYS MATCH THE PARENT: The width of the content should be the same as the parent's width. //It is OK to ignore InlineSurveyView's width
 * -- HEIGHT:
 * -- ALWAYS WRAP THE CONTENT: The height of the content should be the content's height. IT should wrap vertically!!!
 *
 *
 * - WRAP_CONTENT
 * -- WIDTH: ALWAYS MATCH THE PARENT: The width of the content should be the same as the parent's width. //It is OK to ignore InlineSurveyView's width
 * -- HEIGHT:
 * -- ALWAYS WRAP THE CONTENT: The height of the content should be the content's height. IT should wrap vertically!!!
 *
 * Challenges:
 * - Only Android InlineSurveyView can read the parent's size.
 * -- So, it is not possible to read the parent's size at the start.
 * - Only Feeba Web SPA can read the final content height
 *
 *
 * Conclusion:
 * - In Inline mode, Android communicates the Width of the parent to the SPA.
 * - The SPA renders the survey with fixed width that is set by Android
 * - SPA communicates the final height back to Android
 * - Android sets the height of the InlineSurveyView to the final height
 * -- This might cause some undesired behavior if the content is too long. We are OK with this side effect for time being
 *
 */
class InlineSurveyView : FrameLayout {
    private lateinit var webView: FeebaWebView
    private val appHistoryState = FeebaFacade.localStateHolder.readAppHistoryState()

    //Create a constructor that takes in a context
    constructor(context: Context) : super(context) {
        //Call the init function
        init()
    }

    //Create a constructor that takes in a context and an attribute set
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        //Call the init function
        init()
    }

    //Create a constructor that takes in a context, an attribute set and a style
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        //Call the init function
        init()
    }

    //Create a constructor that takes in a context, an attribute set, a style and a default style
    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        //Call the init function
        init()
    }

    //Create a function called init
    private fun init() {
        // Measured width and height are 0 at this point. Validated by Beka
        webView = createWebViewInstanceForManualLoad(
            context, appHistoryState,
            onPageLoaded = { webView, loadType ->
                Logger.d("InlineSurveyView::   onPageLoaded: $loadType")
                if (loadType is PageFrame) {
                    removeAllViews()
                    addView(webView)
                } else if (loadType is SurveyRendered) {
                    // This trigger is not used for now
                } else if (loadType is PageResized) {
                    // There is no need to adjust the width. The width is always MATCH_PARENT
                    // There is no need to adjust the height. The height is always WRAP_CONTENT. Web SPA just wraps the height as well
                }
            },
            onError = {
                // In case of error, remove the view
                removeAllViews()
            },
            // We pass MATCH_PARENT for width and height. The width is always MATCH_PARENT. The height is adjusted by the SPA
            width = FrameLayout.LayoutParams.MATCH_PARENT,
            height = FrameLayout.LayoutParams.WRAP_CONTENT
        )
        isFocusableInTouchMode = true
        requestFocus()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // trigger data push
        webView.evaluateJavascript("window.onInlineViewClosed();", null)
    }

    fun flushResults() {
        // trigger data push
        webView.evaluateJavascript("window.onInlineViewClosed();", null)
    }

    fun loadSurvey(surveyUrl: String) {
        webView.load(surveyUrl, appHistoryState, IntegrationMode.Inline)
    }
}