package io.feeba.ui

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import io.feeba.data.Position
import io.feeba.data.SurveyPresentation
import io.feeba.data.state.AppHistoryState
import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger
import io.feeba.navigationBarHeight
import io.feeba.statusBarHeight

internal class SurveyWebViewHolder(
    private val activity: Activity,
    private val rootView: ViewGroup,
    private val presentation: SurveyPresentation,
    private val appHistoryState: AppHistoryState,
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
                addView(createWebViewInstance(activity, presentation, appHistoryState) {
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
