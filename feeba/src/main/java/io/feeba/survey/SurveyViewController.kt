package io.feeba.survey

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.core.widget.PopupWindowCompat
import io.feeba.Utils
import io.feeba.data.Position.BOTTOM_BANNER
import io.feeba.data.Position.CENTER_MODAL
import io.feeba.data.Position.FULL_SCREEN
import io.feeba.data.Position.TOP_BANNER
import io.feeba.data.SurveyPresentation
import io.feeba.data.isBanner
import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger
import io.feeba.navigationBarHeight
import io.feeba.ui.BannerHolderView
import io.feeba.ui.FloatingView
import io.feeba.ui.ViewUtils


private const val ACTIVITY_INIT_DELAY = 200

internal class SurveyViewController(
    private val content: SurveyPresentation, private var viewLifecycleListener: SurveyViewLifecycleListener
) {
    private var popupWindow: PopupWindow? = null
    private var floatingView: FloatingView? = null

    internal interface SurveyViewLifecycleListener {
        fun onSurveyWasShown()
        fun onSurveyWillDismiss()
        fun onSurveyWasDismissed()
    }

    private val handler = Handler()
    private var marginPxSizeLeft: Int = ViewUtils.dpToPx(24)
    private var marginPxSizeRight: Int = ViewUtils.dpToPx(24)
    private var marginPxSizeTop: Int = ViewUtils.dpToPx(24)
    private var marginPxSizeBottom: Int = ViewUtils.dpToPx(24)
    private val displayDuration: Double = content.displayDuration
    private val hasBackground: Boolean = !isBanner(content.displayLocation)
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
        marginPxSizeTop = if (content.useHeightMargin) ViewUtils.dpToPx(24) else 0
        marginPxSizeBottom = if (content.useHeightMargin) ViewUtils.dpToPx(24) else 0
        marginPxSizeLeft = if (content.useWidthMargin) ViewUtils.dpToPx(24) else 0
        marginPxSizeRight = if (content.useWidthMargin) ViewUtils.dpToPx(24) else 0
    }

    fun start(currentActivity: Activity) {
        Logger.log(LogLevel.DEBUG, "SurveyViewController::showSurvey")
        Utils.runOnMainUIThread {
            if (content.helperKnob != null || false) {
                floatingView = FloatingView(
                    context = currentActivity.applicationContext,
                    rootView = currentActivity.window.decorView.rootView as ViewGroup,
                ) {
                    floatingView?.dismiss()
                    showSurveyUi(currentActivity)
                }

                floatingView?.show()
            } else {
                showSurveyUi(currentActivity)
            }
        }
    }

    private fun showSurveyUi(currentActivity: Activity) {
        val localParentRef = BannerHolderView(currentActivity, content) {
            Utils.runOnMainUIThread {
                popupWindow?.dismiss()
            }
        }
        this.bannerHolderView = localParentRef
        this.popupWindow = createPopupWindow(localParentRef, currentActivity)

//            animateSurvey(localDraggableRef)
        startDismissTimerIfNeeded(currentActivity)
    }

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
            isFocusable = true
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
            showAtLocation(currentActivity.window.decorView.rootView, gravity, 0, currentActivity.navigationBarHeight)

            // Using panel for fullbleed IAMs and dialog for non-fullbleed. The attached dialog type
            // does not allow content to bleed under notches but panel does.
            val displayType = if (content.isFullBleed) WindowManager.LayoutParams.TYPE_APPLICATION_PANEL else WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG
            PopupWindowCompat.setWindowLayoutType(
                this, WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG
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
            start(currentActivity)
            return
        }
        Handler().postDelayed({ delayShowUntilAvailable(currentActivity) }, ACTIVITY_INIT_DELAY.toLong())
    }


//    /**
//     * IAM has been fully dismissed, remove all views and call the onMessageWasDismissed callback
//     */
//    private fun cleanupViewsAfterDismiss() {
//        removeAllViews()
//        viewLifecycleListener.onSurveyWasDismissed()
//    }

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

//    private fun animateAndDismissLayout(backgroundView: View) {
//        val animCallback: Animator.AnimatorListener = object : AnimatorListenerAdapter() {
//            override fun onAnimationEnd(animation: Animator) {
//                cleanupViewsAfterDismiss()
//            }
//        }
//
//        // Animate background behind the message so it hides before being removed from the view
//        animateBackgroundColor(
//            backgroundView, IN_APP_BACKGROUND_ANIMATION_DURATION_MS, ACTIVITY_BACKGROUND_COLOR_FULL, ACTIVITY_BACKGROUND_COLOR_EMPTY, animCallback
//        ).start()
//    }
//
//    private fun animateBackgroundColor(backgroundView: View, duration: Int, startColor: Int, endColor: Int, animCallback: Animator.AnimatorListener?): ValueAnimator {
//        return AnimationUtils.animateViewColor(
//            backgroundView, duration, startColor, endColor, animCallback
//        )
//    }
}