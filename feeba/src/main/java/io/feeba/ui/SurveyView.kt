package io.feeba.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import io.feeba.FeebaFacade
import io.feeba.appendQueryParameter

//Create a custom view SurveyView that extends FrameLayout
class SurveyView : FrameLayout {
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
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        //Call the init function
        init()
    }

    //Create a constructor that takes in a context, an attribute set, a style and a default style
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        //Call the init function
        init()
    }

    //Create a function called init
    private fun init() {
        webView = createWebViewInstance(context, appHistoryState,
            onPageLoaded = { webView, loadType ->
                removeAllViews()
                addView(webView)
            },
            onError = {
                // In case of error, remove the view
                removeAllViews()
            }
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
//        webView.evaluateJavascript("window.onInlineViewClosed();", null)
    }

    fun loadSurvey(surveyUrl: String) {
        webView.loadUrl(appendQueryParameter(surveyUrl, "lang", appHistoryState.userData.langCode ?: "en"))
    }
}