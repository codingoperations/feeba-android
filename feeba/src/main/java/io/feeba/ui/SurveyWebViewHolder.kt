package io.feeba.ui

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.http.SslError
import android.os.Build
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import io.feeba.data.Position
import io.feeba.data.SurveyPresentation
import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger
import io.feeba.navigationBarHeight
import io.feeba.statusBarHeight
import io.feeba.survey.CallToAction
import io.feeba.survey.JsInterface

internal class SurveyWebViewHolder(
    private val activity: Activity,
    private val rootView: ViewGroup,
    private val presentation: SurveyPresentation,
    private val onSurveyClose: () -> Unit
) {
    private val mContentLayout: View
    private var dismissed = false

    init {
        mContentLayout = createContentView()
    }

    fun show() {
        Logger.log(LogLevel.DEBUG, "SurveyWebViewHolder::show")
        verifyDismissed()
        rootView.removeView(mContentLayout)
        rootView.addView(mContentLayout)
    }

    private fun verifyDismissed() {
        if (!dismissed) {
            Logger.log(LogLevel.ERROR, "Tooltip has been dismissed.")
        }
    }

    private fun createContentView(): View {
        return FrameLayout(activity).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setPadding(0, activity.statusBarHeight, 0, activity.navigationBarHeight)

            val ll = this

            setOnTouchListener(object : View.OnTouchListener {
                private var touchStartTime: Long = Long.MAX_VALUE
                private var coordinatesDelta: IntArray? = null
                private var isDragging = false

                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    val x = event.rawX.toInt()
                    val y = event.rawY.toInt()

                    Logger.log(LogLevel.DEBUG, "onTouch: x=$x, y=$y")
                    Logger.log(LogLevel.DEBUG, "View size: ${v.width}x${v.height}")
                    Logger.log(LogLevel.DEBUG, "View name: ${v.javaClass.simpleName}")
                    return when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            // calculate X and Y coordinates of view relative to screen
                            val viewLocation = IntArray(2)
                            v.getLocationOnScreen(viewLocation)
                            Logger.log(LogLevel.DEBUG, "View on Screen: x=${viewLocation[0]} y=${viewLocation[1]}")
                            coordinatesDelta = intArrayOf(x - viewLocation[0], y - viewLocation[1])
                            coordinatesDelta?.let {
                                Logger.log(LogLevel.DEBUG, "coordinatesDelta: x=${it[0]} y=${it[1]}")
                            }
                            // Record the start time of the touch event
                            touchStartTime = System.currentTimeMillis();

                            true // Important to return false so the touch event isn't consumed and is passed to children
                        }

                        MotionEvent.ACTION_MOVE -> {
                            // Calculate new position of the PopupWindow
                            val newX = event.rawX - (coordinatesDelta?.getOrElse(0) { 0 } ?: 0)
                            val newY = event.rawY - (coordinatesDelta?.getOrElse(1) { 0 } ?: 0)

                            // Update the position of the PopupWindow
                            ll.x = newX
                            ll.y = newY
                            isDragging = true
                            true
                        }

                        MotionEvent.ACTION_UP -> {
                            // Add any additional logic for when the drag is released if necessary
                            coordinatesDelta = null
                            if (isDragging) {
                                isDragging = false
                                return true // terminate the responder chain
                            }
                            Logger.log(LogLevel.DEBUG, "onTouch: ACTION_UP")
                            if (System.currentTimeMillis() - touchStartTime < ViewConfiguration.getTapTimeout()) {
                                // Consider as a click event
                                dismiss()
                            }
                            false
                        }

                        else -> false
                    }
                }
            })

            addView(createCardView(activity, presentation).apply {
                addView(createWebViewInstance(activity, presentation) {
                    dismiss()
                })
            })
            isFocusableInTouchMode = true
            requestFocus()
            setOnKeyListener { v, keyCode, event ->
                Logger.log(LogLevel.DEBUG, "onKeyListener: keyCode=$keyCode, event=$event")
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss()
                    return@setOnKeyListener true
                }
                return@setOnKeyListener false
            }
        }
    }

    fun dismiss() {
        if (dismissed) return
        dismissed = true
        rootView.removeView(mContentLayout)
        mContentLayout.visibility = View.GONE
        onSurveyClose()
    }
}


/**
 * To show drop shadow on WebView
 * Layout container for WebView is needed
 */
private fun createCardView(activity: Activity, content: SurveyPresentation): CardView {
    val cardView = CardView(activity.applicationContext).apply {
        val adjustedHeight: Int = if (content.maxWidgetHeightInPercent in 1..100) content.maxWidgetHeightInPercent else 70
        val height = ViewUtils.getWindowHeight(activity) * (adjustedHeight / 100f)
        val adjustedWidth: Int = if (content.maxWidgetWidthInPercent in 1..100) content.maxWidgetWidthInPercent else 90
        val width = ViewUtils.getWindowWidth(activity) * (adjustedWidth / 100f)
        Logger.log(LogLevel.DEBUG, "Activity height -> ${ViewUtils.getWindowHeight(activity)}, width -> ${ViewUtils.getWindowWidth(activity)}")
        Logger.log(LogLevel.DEBUG, "createCardView::height: $height, width: $width")
        FrameLayout.LayoutParams(width.toInt(), height.toInt()).also {
            when (content.displayLocation) {
                Position.TOP_BANNER -> {
                    it.gravity = Gravity.CENTER or Gravity.TOP
                }

                Position.BOTTOM_BANNER -> {
                    it.gravity = Gravity.CENTER or Gravity.BOTTOM
                }

                Position.FULL_SCREEN -> {
                    it.gravity = Gravity.CENTER
                    it.height = FrameLayout.LayoutParams.MATCH_PARENT
                    it.width = FrameLayout.LayoutParams.MATCH_PARENT
                }

                Position.CENTER_MODAL -> {
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
    cardView.setCardBackgroundColor(Color.WHITE)
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