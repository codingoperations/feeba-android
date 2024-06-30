package io.feeba.ui

import android.content.Context
import android.webkit.WebView
import io.feeba.appendQueryParameter
import io.feeba.data.SurveyPresentation
import io.feeba.data.state.AppHistoryState
import io.feeba.lifecycle.Logger

// Custom WebView to lock scrolling
class FeebaWebView(context: Context, maxWidthPercentage: Int = 100, maxHeightPercentage: Int = 100) : WebView(context) {
    private val maxWidthPx = resources.displayMetrics.widthPixels * maxWidthPercentage / 100
    private val maxHeightPx = resources.displayMetrics.heightPixels * maxHeightPercentage / 100

    init {
        overScrollMode = OVER_SCROLL_NEVER
    }

    @Deprecated("Deprecated in Java")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Logger.d("FeebaWebView::onMeasure: $widthMeasureSpec, $heightMeasureSpec")
        // Adjust width as necessary
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        if (maxWidthPx in 1..<measuredWidth) {
            val measureMode = MeasureSpec.getMode(widthMeasureSpec)
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(maxWidthPx, measureMode)
        }
        // Adjust height as necessary
        val measuredHeight = MeasureSpec.getSize(heightMeasureSpec)
        if (maxHeightPx in 1..<measuredHeight) {
            val measureMode = MeasureSpec.getMode(heightMeasureSpec)
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeightPx, measureMode)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    @Deprecated("Deprecated. This function is not supported.", ReplaceWith("load(url, headers)"))
    override fun loadUrl(url: String) {
        Logger.w("Do not invoke this call. Deprecated! Call loadUrl(url, headers) instead.")
    }

    fun load(originalUrl: String, appHistoryState: AppHistoryState, integrationMode: IntegrationMode) {
        val url = appendQueryParameter(
            originalUrl,
            arrayOf(
                Pair("lang", appHistoryState.userData?.langCode ?: "en"),
                Pair("im", integrationMode.toString())
            )
        )
        super.loadUrl(url)
    }
}