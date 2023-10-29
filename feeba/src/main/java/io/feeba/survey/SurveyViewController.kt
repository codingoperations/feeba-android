package io.feeba.survey

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.core.widget.PopupWindowCompat
import io.feeba.Feeba
import io.feeba.Utils
import io.feeba.data.Position.BOTTOM_BANNER
import io.feeba.data.Position.CENTER_MODAL
import io.feeba.data.Position.FULL_SCREEN
import io.feeba.data.Position.TOP_BANNER
import io.feeba.data.SurveyPresentation
import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger
import io.feeba.ui.AnimationUtils
import io.feeba.ui.BannerHolderView
import io.feeba.ui.ViewUtils
import io.least.ui.dpToPx

/**
 * Layout Documentation
 * ### Modals & Banners ###
 * - WebView
 * - width  = MATCH_PARENT
 * - height = PX height provided via a JS event for the content
 * - Parent Layouts
 * - width  = MATCH_PARENT
 * - height = WRAP_CONTENT - Since the WebView is providing the height.
 * ### Fullscreen ###
 * - WebView
 * - width  = MATCH_PARENT
 * - height = MATCH_PARENT
 * - Parent Layouts
 * - width  = MATCH_PARENT
 * - height = MATCH_PARENT
 */
private val ACTIVITY_BACKGROUND_COLOR_EMPTY = Color.parseColor("#00000000")
private val ACTIVITY_BACKGROUND_COLOR_FULL = Color.parseColor("#BB000000")
private const val IN_APP_BANNER_ANIMATION_DURATION_MS = 1000
private const val IN_APP_CENTER_ANIMATION_DURATION_MS = 1000
private const val IN_APP_BACKGROUND_ANIMATION_DURATION_MS = 400
private const val ACTIVITY_FINISH_AFTER_DISMISS_DELAY_MS = 600
private const val ACTIVITY_INIT_DELAY = 200

internal class SurveyViewController(
    private val content: SurveyPresentation, private var viewLifecycleListener: SurveyViewLifecycleListener
) {
    private var popupWindow: PopupWindow? = null

    internal interface SurveyViewLifecycleListener {
        fun onSurveyWasShown()
        fun onSurveyWillDismiss()
        fun onSurveyWasDismissed()
    }

    private val handler = Handler()
    private var marginPxSizeLeft: Int = dpToPx(24f)
    private var marginPxSizeRight: Int = dpToPx(24f)
    private var marginPxSizeTop: Int = dpToPx(24f)
    private var marginPxSizeBottom: Int = dpToPx(24f)
    private val displayDuration: Double = content.displayDuration
    private val hasBackground: Boolean = !content.displayLocation.isBanner()
    private var shouldDismissWhenActive = false

    private var bannerHolderView: BannerHolderView? = null
    private var scheduleDismissRunnable: Runnable? = null


    init {
        setMarginsFromContent(content)
    }

    /**
     * For now we only support default margin or no margin.
     * Any non-zero value will be treated as default margin
     * @param content in app message content and style
     */
    private fun setMarginsFromContent(content: SurveyPresentation) {
        marginPxSizeTop = if (content.useHeightMargin) dpToPx(24f) else 0
        marginPxSizeBottom = if (content.useHeightMargin) dpToPx(24f) else 0
        marginPxSizeLeft = if (content.useWidthMargin) dpToPx(24f) else 0
        marginPxSizeRight = if (content.useWidthMargin) dpToPx(24f) else 0
    }


    fun showView(activity: Activity) {
        delayShowUntilAvailable(activity)
    }


    /**
     * This will fired when the device is rotated for example with a new provided height for the WebView
     * Called to shrink or grow the WebView when it receives a JS resize event with a new height.
     *
     * @param pageHeight the provided height
     */
    fun updateHeight(pageHeight: Int, currentActivity: Activity) {
//        this.pageHeight = pageHeight
//        Utils.runOnMainUIThread(Runnable {
//            val layoutParams = webView.layoutParams
//            if (layoutParams == null) {
//                Logger.log(
//                    LogLevel.WARN,
//                    "WebView height update skipped because of null layoutParams, new height will be used once it is displayed."
//                )
//                return@Runnable
//            }
//            layoutParams.height = pageHeight
//            // We only need to update the WebView size since it's parent layouts are set to
//            //   WRAP_CONTENT to always match the height of the WebView. (Expect for fullscreen)
//            webView.layoutParams = layoutParams
//
//            // draggableRelativeLayout comes in null here sometimes, this is due to the IAM
//            //  not being ready to be shown yet
//            // When preparing the IAM, the correct height will be set and handle this job, so
//            //  all bases are covered and the draggableRelativeLayout will never have the wrong height
//            if (draggableRelativeLayout != null) draggableRelativeLayout?.setParams(
//                createDraggableLayoutParams(
//                    pageHeight,
//                    displayLocation,
//                    disableDragDismiss,
//                    ViewUtils.getWindowHeight(currentActivity)
//                )
//            )
//        })
    }

    fun showSurvey(currentActivity: Activity) {
        Logger.log(LogLevel.DEBUG, "SurveyViewController::showSurvey")
        Utils.runOnMainUIThread {
            val localParentRef =  BannerHolderView(currentActivity, content) {
                Utils.runOnMainUIThread {
                    popupWindow?.dismiss()
                }
            }
            this.bannerHolderView = localParentRef
            this.popupWindow = createPopupWindow(localParentRef, currentActivity)

//            animateSurvey(localDraggableRef)
            startDismissTimerIfNeeded(currentActivity)
        }
    }


//    private fun createDraggableLayoutParams(pageHeight: Int, windowHeight: Int): BannerHolderView.Params {
//        var pageHeight = pageHeight
//        val draggableParams = BannerHolderView.Params(
//            maxXPos = marginPxSizeRight,
//            maxYPos = marginPxSizeTop,
//            messageHeight = pageHeight,
//            height = windowHeight,
//        )
//
//        when (content.displayLocation) {
//            TOP_BANNER -> draggableParams.dragThresholdY = marginPxSizeTop
//            BOTTOM_BANNER -> {
//                draggableParams.posY = windowHeight - pageHeight
//            }
//
//            FULL_SCREEN -> {
//                run {
//                    pageHeight = windowHeight - (marginPxSizeBottom + marginPxSizeTop)
//                    draggableParams.messageHeight = pageHeight
//                }
//                val y = windowHeight / 2 - pageHeight / 2
//                draggableParams.maxYPos = y
//                draggableParams.posY = y
//            }
//
//            CENTER_MODAL -> {
//                val y = windowHeight / 2 - pageHeight / 2
//                draggableParams.maxYPos = y
//                draggableParams.posY = y
//            }
//        }
//        return draggableParams
//    }



    /**
     * Create a new Android PopupWindow that draws over the current Activity
     *
     * @param parentLayout root layout to attach to the pop up window
     */
    private fun createPopupWindow(parentLayout: BannerHolderView, currentActivity: Activity): PopupWindow {
        return PopupWindow(
            parentLayout,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            true
        ).apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isTouchable = true
            // NOTE: This is required for getting fullscreen under notches working in portrait mode
            isClippingEnabled = false
            var gravity = Gravity.CENTER_HORIZONTAL
            if (!hasBackground) {
                gravity = when (content.displayLocation) {
                    TOP_BANNER -> Gravity.CENTER_HORIZONTAL or Gravity.TOP
                    BOTTOM_BANNER -> Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                    CENTER_MODAL, FULL_SCREEN -> Gravity.CENTER_HORIZONTAL
                }
            }
            setOnDismissListener {
                viewLifecycleListener.onSurveyWillDismiss()
                viewLifecycleListener.onSurveyWasDismissed()
            }
            showAtLocation(currentActivity.window.decorView.rootView, gravity, 0, 0)

            // Using panel for fullbleed IAMs and dialog for non-fullbleed. The attached dialog type
            // does not allow content to bleed under notches but panel does.
            val displayType = if (content.isFullBleed) WindowManager.LayoutParams.TYPE_APPLICATION_PANEL else WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG
            PopupWindowCompat.setWindowLayoutType(
                this, displayType
            )
        }
    }


    /**
     * Schedule dismiss behavior, if IAM has a dismiss after X number of seconds timer.
     */
    private fun startDismissTimerIfNeeded(currentActivity: Activity) {
        if (displayDuration <= 0) return

        if (scheduleDismissRunnable == null) {
            val scheduleDismissRunnable = Runnable {
                viewLifecycleListener.onSurveyWillDismiss()
                if (currentActivity != null) {
                    scheduleDismissRunnable = null
                } else {
                    // For cases when the app is on background and the dismiss is triggered
                    shouldDismissWhenActive = true
                }
            }
            this.scheduleDismissRunnable = scheduleDismissRunnable
            handler.postDelayed(scheduleDismissRunnable, displayDuration.toLong() * 1000)
        }
    }

    // Do not add view until activity is ready
    private fun delayShowUntilAvailable(currentActivity: Activity) {
        if (ViewUtils.isActivityFullyReady(currentActivity) && bannerHolderView == null) {
            showSurvey(currentActivity)
            return
        }
        Handler().postDelayed({ delayShowUntilAvailable(currentActivity) }, ACTIVITY_INIT_DELAY.toLong())
    }

    /**
     * Finishing on a timer as continueSettling does not return false
     * when using smoothSlideViewTo on Android 4.4
     */
    private fun finishAfterDelay() {
        Utils.runOnMainThreadDelayed(Runnable {
            bannerHolderView?.let {
                if (hasBackground) {
                    animateAndDismissLayout(it)
                } else {
                    cleanupViewsAfterDismiss()
                }
            } ?: cleanupViewsAfterDismiss()

        }, ACTIVITY_FINISH_AFTER_DISMISS_DELAY_MS)
    }

    /**
     * IAM has been fully dismissed, remove all views and call the onMessageWasDismissed callback
     */
    private fun cleanupViewsAfterDismiss() {
        removeAllViews()
        viewLifecycleListener.onSurveyWasDismissed()
    }

    /**
     * Remove all views and dismiss PopupWindow
     */
    fun removeAllViews() {
        Logger.log(LogLevel.DEBUG, "InAppMessageView removing views")
        if (scheduleDismissRunnable != null) {
            // Dismissed before the dismiss delay
            handler.removeCallbacks(scheduleDismissRunnable!!)
            scheduleDismissRunnable = null
        }
        bannerHolderView?.removeAllViews()
        popupWindow?.dismiss()
        dereferenceViews()
    }

    /**
     * Cleans all layout references so this can be cleaned up in the next GC
     */
    private fun dereferenceViews() {
        // Dereference so this can be cleaned up in the next GC
        bannerHolderView = null
    }

//    private fun animateSurvey(messageView: View) {
//        val messageViewCardView: CardView = messageView.findViewWithTag<CardView>(IN_APP_MESSAGE_CARD_VIEW_TAG)
//        val cardViewAnimCallback = createAnimationListener(messageViewCardView)
//        when (content.displayLocation) {
//            TOP_BANNER -> animateTop(messageViewCardView, webView.height, cardViewAnimCallback)
//            BOTTOM_BANNER -> animateBottom(messageViewCardView, webView.height, cardViewAnimCallback)
//            CENTER_MODAL, FULL_SCREEN -> animateCenter(messageView, backgroundView, cardViewAnimCallback, null)
//        }
//    }
//
//    private fun createAnimationListener(messageViewCardView: CardView): Animation.AnimationListener {
//        return object : Animation.AnimationListener {
//            override fun onAnimationStart(animation: Animation) {}
//            override fun onAnimationEnd(animation: Animation) {
//                // For Android 6 API 23 devices, waits until end of animation to set elevation of CardView class
//                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
//                    messageViewCardView.cardElevation = dpToPx(5f).toFloat()
//                }
//                viewLifecycleListener.onSurveyWasShown()
//            }
//
//            override fun onAnimationRepeat(animation: Animation) {}
//        }
//    }
//
//    private fun animateTop(messageView: View, height: Int, cardViewAnimCallback: Animation.AnimationListener) {
//        // Animate the message view from above the screen downward to the top
//        AnimationUtils.animateViewByTranslation(
//            messageView, (-height - marginPxSizeTop).toFloat(), 0f, IN_APP_BANNER_ANIMATION_DURATION_MS, FeebaBounceInterpolator(0.1, 8.0), cardViewAnimCallback
//        ).start()
//    }
//
//    private fun animateBottom(messageView: View, height: Int, cardViewAnimCallback: Animation.AnimationListener) {
//        // Animate the message view from under the screen upward to the bottom
//        AnimationUtils.animateViewByTranslation(
//            messageView, (height + marginPxSizeBottom).toFloat(), 0f, IN_APP_BANNER_ANIMATION_DURATION_MS, FeebaBounceInterpolator(0.1, 8.0), cardViewAnimCallback
//        ).start()
//    }
//
//    private fun animateCenter(messageView: View, backgroundView: View, cardViewAnimCallback: Animation.AnimationListener, backgroundAnimCallback: Animator.AnimatorListener?) {
//        // Animate the message view by scale since it settles at the center of the screen
//        val messageAnimation: Animation = AnimationUtils.animateViewSmallToLarge(
//            messageView, IN_APP_CENTER_ANIMATION_DURATION_MS, FeebaBounceInterpolator(0.1, 8.0), cardViewAnimCallback
//        )
//
//        // Animate background behind the message so it doesn't just show the dark transparency
//        val backgroundAnimation = animateBackgroundColor(
//            backgroundView, IN_APP_BACKGROUND_ANIMATION_DURATION_MS, ACTIVITY_BACKGROUND_COLOR_EMPTY, ACTIVITY_BACKGROUND_COLOR_FULL, backgroundAnimCallback
//        )
//        messageAnimation.start()
//        backgroundAnimation.start()
//    }

    private fun animateAndDismissLayout(backgroundView: View) {
        val animCallback: Animator.AnimatorListener = object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                cleanupViewsAfterDismiss()
            }
        }

        // Animate background behind the message so it hides before being removed from the view
        animateBackgroundColor(
            backgroundView, IN_APP_BACKGROUND_ANIMATION_DURATION_MS, ACTIVITY_BACKGROUND_COLOR_FULL, ACTIVITY_BACKGROUND_COLOR_EMPTY, animCallback
        ).start()
    }

    private fun animateBackgroundColor(backgroundView: View, duration: Int, startColor: Int, endColor: Int, animCallback: Animator.AnimatorListener?): ValueAnimator {
        return AnimationUtils.animateViewColor(
            backgroundView, duration, startColor, endColor, animCallback
        )
    }

    override fun toString(): String {
        return "InAppMessageView{" + ", displayDuration=" + displayDuration + ", hasBackground=" + hasBackground + ", shouldDismissWhenActive=" + shouldDismissWhenActive + ", displayLocation=" + content.displayLocation
    }

}