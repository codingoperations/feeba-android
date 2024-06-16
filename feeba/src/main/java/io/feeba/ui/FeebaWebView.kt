package io.feeba.ui

import android.content.Context
import android.webkit.WebView

// Custom WebView to lock scrolling
class FeebaWebView(context: Context, maxWidthPercentage: Int = 100, maxHeightPercentage: Int = 100) : WebView(context) {
    private val maxWidthPx = resources.displayMetrics.widthPixels * maxWidthPercentage / 100
    private val maxHeightPx = resources.displayMetrics.heightPixels * maxHeightPercentage / 100

    init {
        overScrollMode = OVER_SCROLL_NEVER
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
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
}