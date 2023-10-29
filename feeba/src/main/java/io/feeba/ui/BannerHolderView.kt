package io.feeba.ui

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.RelativeLayout
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
import io.least.ui.dpToPx


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
    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) cardView.cardElevation = 0f else cardView.cardElevation = dpToPx(5f).toFloat()
    cardView.radius = dpToPx(8f).toFloat()
    cardView.clipChildren = false
    cardView.clipToPadding = false
    cardView.preventCornerOverlap = false
    cardView.setCardBackgroundColor(Color.TRANSPARENT)
    return cardView
}

private fun createWebViewInstance(context: Context, presentation: SurveyPresentation, onOutsideTouch: (() -> Unit)?): FeebaWebView {
    return FeebaWebView(context).apply {
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
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
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
                Logger.log(LogLevel.DEBUG, "WebViewClient::onPageStarted")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Logger.log(LogLevel.DEBUG, "WebViewClient::onPageFinished")
            }
        }
        loadUrl(presentation.surveyWebAppUrl)
    }
}
