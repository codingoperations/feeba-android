package io.feeba.ui

import android.app.Activity
import android.content.Context
import android.view.View.MeasureSpec.EXACTLY
import android.webkit.WebView
import io.feeba.appendQueryParameter
import io.feeba.data.state.AppHistoryState
import io.feeba.lifecycle.Logger
import kotlin.math.max
import kotlin.math.min

// Custom WebView to lock scrolling
class FeebaWebView(context: Context, maxWidthPercent: Int = 100, maxHeightPercent: Int = 100, minWidthPercent: Int = 1, minHeightPercent: Int = 1) : WebView(context) {
    private val maxWidthPx = resources.displayMetrics.widthPixels * maxWidthPercent / 100
    private val maxHeightPx = resources.displayMetrics.heightPixels * maxHeightPercent / 100
    private val minWidthPx = resources.displayMetrics.widthPixels * minWidthPercent / 100
    private val minHeightPx = resources.displayMetrics.heightPixels * minHeightPercent / 100


    init {
        overScrollMode = OVER_SCROLL_NEVER
    }

    @Deprecated("Deprecated in Java")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Adjust width as necessary
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        // Adjust width as necessary to ensure it is within the max and min width
//        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
//        val adjustedWidth = min(maxWidthPx, max(minWidthPx, widthSize))
//        widthMeasureSpec = MeasureSpec.makeMeasureSpec(adjustedWidth, EXACTLY)
//
//        // Adjust height as necessary to ensure it is within the max and min height
//        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
//        val adjustedHeight = min(maxHeightPx, max(minHeightPx, heightSize))
//        heightMeasureSpec = MeasureSpec.makeMeasureSpec(adjustedHeight, EXACTLY)
//
//        Logger.d("FeebaWebView:: h=${heightSize}, maxH=${maxHeightPx}, minH=${minHeightPx}, adjustedH=${adjustedHeight}")
//        Logger.d("FeebaWebView:: w=${widthSize}, maxW=${maxWidthPx}, minW=${minWidthPx}, adjustedW=${adjustedWidth}")

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Logger.d("FeebaWebView::onMeasure:measured => $measuredWidth, $measuredHeight")
    }

    @Deprecated("Deprecated. This function is not supported.", ReplaceWith("load(url, headers)"))
    override fun loadUrl(url: String) {
        Logger.w("Do not invoke this call. Deprecated! Call loadUrl(url, headers) instead.")
    }

    fun load(originalUrl: String, appHistoryState: AppHistoryState, integrationMode: IntegrationMode) {
        val queryParamsArray = mutableListOf(
            Pair("lang", appHistoryState.userData?.langCode ?: "en"),
            Pair("im", integrationMode.toString()),
            // Breakpoint
            Pair("bp", readCssBreakPointValue(context as Activity)),
        )

        try {
            val maxWidthWebPixels = maxWidthPx / resources.displayMetrics.density
            val minWidthWebPixels = minWidthPx / resources.displayMetrics.density
            queryParamsArray.add(Pair("mxw", maxWidthWebPixels.toString()))
            queryParamsArray.add(Pair("mnw", minWidthWebPixels.toString()))
        } catch (e: Exception) {
            Logger.e("Error while adding max width to query params: ${e.message}")
        }


        val url = appendQueryParameter(
            originalUrl,
            queryParamsArray
        )
        super.loadUrl(url)
    }
}