package io.feeba.ui

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.http.SslError
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import io.feeba.data.Position.BOTTOM_BANNER
import io.feeba.data.Position.CENTER_MODAL
import io.feeba.data.Position.FULL_SCREEN
import io.feeba.data.Position.TOP_BANNER
import io.feeba.data.SurveyPresentation
import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger
import io.feeba.survey.CallToAction
import io.feeba.survey.JsInterface


private const val IN_APP_MESSAGE_CARD_VIEW_TAG = "IN_APP_MESSAGE_CARD_VIEW_TAG"

internal class BannerHolderView(activity: Activity, private val presentation: SurveyPresentation, private val onOutsideTouch: (() -> Unit)? = null) : FrameLayout(activity.applicationContext) {

    init {
        clipChildren = false
        clipToPadding = false
        // Setting layout params
        this.layoutParams = LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        background = ColorDrawable(Color.TRANSPARENT)

        addView(createCardView(activity, presentation).apply {
            tag = IN_APP_MESSAGE_CARD_VIEW_TAG
            addView(createWebViewInstance(context, presentation, onOutsideTouch))
        })
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        // Iterate through child views to check if the touch is inside any of them
        var isChildViewTouched = false
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (isPointInsideView(event.rawX, event.rawY, child)) {
                // If the touch is inside a child view, intercept the event
                isChildViewTouched = true
            }
        }
        if (!isChildViewTouched) onOutsideTouch?.invoke()
        // If the touch is not inside any child view, do not intercept the event
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Handle touch events here if they are not intercepted
        return true
    }
}

// Helper method to check if a point (x, y) is inside a view
private fun isPointInsideView(x: Float, y: Float, view: View): Boolean {
    val location = IntArray(2)
    view.getLocationOnScreen(location)
    val viewX = location[0]
    val viewY = location[1]
    val viewRight = viewX + view.width
    val viewBottom = viewY + view.height
    return x >= viewX && x <= viewRight && y >= viewY && y <= viewBottom
}

/**
 * To show drop shadow on WebView
 * Layout container for WebView is needed
 */
private fun createCardView(activity: Activity, content: SurveyPresentation): CardView {
    val cardView = CardView(activity.applicationContext).apply {
        val adjustedHeight: Int = if (content.maxWidgetHeightInPercent in 1..100) content.maxWidgetHeightInPercent else 70
        val height = ViewUtils.getWindowHeight(activity) * (adjustedHeight / 100f)
        val adjustedWidth: Int = if (content.maxWidgetHeightInPercent in 1..100) content.maxWidgetHeightInPercent else 90
        val width = ViewUtils.getWindowWidth(activity) * (adjustedWidth / 100f)
        Logger.log(LogLevel.DEBUG, "Activity height -> ${ViewUtils.getWindowHeight(activity)}")
        Logger.log(LogLevel.DEBUG, "createCardView::height: $height, width: $width")
        FrameLayout.LayoutParams(width.toInt(), height.toInt()).also {
            when (content.displayLocation) {
                TOP_BANNER -> {
                    it.gravity = Gravity.CENTER or Gravity.TOP
                }

                BOTTOM_BANNER -> {
                    it.gravity = Gravity.CENTER or Gravity.BOTTOM
                }

                FULL_SCREEN -> {
                    it.gravity = Gravity.CENTER
                    it.height = FrameLayout.LayoutParams.MATCH_PARENT
                    it.width = FrameLayout.LayoutParams.MATCH_PARENT
                }

                CENTER_MODAL -> {
                    it.gravity = Gravity.CENTER
                }
            }
            layoutParams = it
        }
    }

    // Set the initial elevation of the CardView to 0dp if using Android 6 API 23
    //  Fixes bug when animating a elevated CardView class
    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) cardView.cardElevation = 0f else cardView.cardElevation = ViewUtils.dpToPx(5).toFloat()
    cardView.radius = ViewUtils.dpToPx(8).toFloat()
    cardView.clipChildren = false
    cardView.clipToPadding = false
    cardView.preventCornerOverlap = false
    cardView.setCardBackgroundColor(Color.TRANSPARENT)
    return cardView
}

private fun createWebViewInstance(context: Context, presentation: SurveyPresentation, onOutsideTouch: (() -> Unit)?): FeebaWebView {
    return FeebaWebView(context).apply {
//        WebView.setWebContentsDebuggingEnabled(true);
        layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT).apply {
            addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
            addRule(RelativeLayout.CENTER_HORIZONTAL)
        }
        setBackgroundColor(Color.TRANSPARENT)
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            allowFileAccess = true

            domStorageEnabled = true
            // Below is trying to fetch a JS bundle that is outdated. Requires deeper investigation
//            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        }
        addJavascriptInterface(JsInterface(context) {
            when (it) {
                CallToAction.CLOSE_SURVEY -> {
                    Logger.log(LogLevel.DEBUG, "FeebaWebView::JsInterface::CallToAction.CLOSE_SURVEY")
                    onOutsideTouch?.invoke()
                }
            }
        }, "Mobile")
        webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                Logger.log(LogLevel.DEBUG, "WebViewClient::onPageStarted, url: $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Logger.log(LogLevel.DEBUG, "WebViewClient::onPageFinished, url: $url")
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError) {
                Logger.log(LogLevel.ERROR, "WebViewClient::onReceivedError, error: $error")
                // Log WebView errors here
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Log error details on API level 23 and above
                    Logger.log(LogLevel.ERROR, "WebViewClient::onReceivedError, description: ${error.description}")
                } else {
                    // Log error details on API level below 23
                    Logger.log(LogLevel.ERROR, "WebViewClient::onReceivedError, description: ${error}")
                }
            }

            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
                super.onReceivedHttpError(view, request, errorResponse)
                Logger.log(LogLevel.ERROR, "WebViewClient::onReceivedHttpError, errorResponse: $errorResponse")
                Logger.log(LogLevel.ERROR, "WebViewClient::onReceivedHttpError, statusCode: ${errorResponse.statusCode}")
                Logger.log(LogLevel.ERROR, "WebViewClient::onReceivedHttpError, reasonPhrase: ${errorResponse.reasonPhrase}")
                Logger.log(LogLevel.ERROR, "WebViewClient::onReceivedHttpError, headers: ${errorResponse.responseHeaders}")
//                Logger.log(LogLevel.ERROR, "WebViewClient::onReceivedHttpError, data: ${errorResponse.data.use { it.reader().readText() } }}")
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                super.onReceivedSslError(view, handler, error)
                Logger.log(LogLevel.ERROR, "WebViewClient::onReceivedSslError, error: $error")
            }
        }
        webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Logger.log(LogLevel.DEBUG, "WebChromeClient::onConsoleMessage, message: ${consoleMessage.message()}")
                Logger.log(LogLevel.DEBUG, "WebChromeClient::onConsoleMessage full: $consoleMessage")

                return true
            }

        }
        loadUrl(presentation.surveyWebAppUrl)
    }
}
