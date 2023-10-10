package io.feeba.lifecycle

import android.app.Activity
import android.webkit.JavascriptInterface
import io.feeba.data.Position
import io.feeba.data.SurveyPresentation
import io.feeba.ui.FeebaWebView
import org.json.JSONException
import org.json.JSONObject

// Manages WebView instances by pre-loading them, displaying them, and closing them when dismissed.
//   Includes a static map for pre-loading, showing, and dismissed so these events can't be duplicated.
// Flow for Displaying WebView
// 1. showHTMLString - Creates WebView and loads page.
// 2. Wait for JavaScriptInterface.postMessage to fire with "rendering_complete"
// 3. This calls showActivity which starts a new WebView
// 4. WebViewActivity will call WebViewManager.instanceFromIam(...) to get this instance and
//       add it's prepared WebView add add it to the Activity.
internal class WebViewManager(activity: Activity, messageContent: SurveyPresentation) {
    private val messageViewSyncLock: Any = object : Any() {}

    private val webView: FeebaWebView? = null

//    private val messageView: InAppMessageView? = null

    private val currentActivityName: String? = null
    private val lastPageHeight: Int? = null

    // dismissFired prevents onDidDismiss from getting called multiple times
    private val dismissFired = false

    // closing prevents IAM being redisplayed when the activity changes during an actionHandler
    private var closing = false

    internal interface OneSignalGenericCallback {
        fun onComplete()
    }


    // Lets JS from the page send JSON payloads to this class
    internal inner class FeebaJavaScriptInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
//            try {
//                Logger.log(LogLevel.DEBUG, "OSJavaScriptInterface:postMessage: $message")
//                val jsonObject = JSONObject(message)
//                val messageType: String = jsonObject.getString(Companion.EVENT_TYPE_KEY)
//                when (messageType) {
//                    Companion.EVENT_TYPE_RENDERING_COMPLETE -> handleRenderComplete(jsonObject)
//                    Companion.EVENT_TYPE_ACTION_TAKEN ->                         // Added handling so that click actions won't trigger while dragging the IAM
//                        if (!messageView.isDragging()) handleActionTaken(jsonObject)
//
//                    Companion.EVENT_TYPE_RESIZE -> {}
//                    Companion.EVENT_TYPE_PAGE_CHANGE -> handlePageChange(jsonObject)
//                    else -> {}
//                }
//            } catch (e: JSONException) {
//                e.printStackTrace()
//            }
        }

        private fun handleRenderComplete(jsonObject: JSONObject) {
//            val displayType: Position = getDisplayLocation(jsonObject)
//            val pageHeight = if (displayType === Position.FULL_SCREEN) -1 else getPageHeightData(jsonObject)
//            val dragToDismissDisabled = getDragToDismissDisabled(jsonObject)
//            messageContent.setDisplayLocation(displayType)
//            messageContent.setPageHeight(pageHeight)
//            createNewInAppMessageView(dragToDismissDisabled)
        }

        private fun getPageHeightData(jsonObject: JSONObject): Int {
//            return try {
//                pageRectToViewHeight(activity, jsonObject.getJSONObject(Companion.IAM_PAGE_META_DATA_KEY))
//            } catch (e: JSONException) {
//                -1
//            }
            return -1
        }


        private fun getDisplayLocation(jsonObject: JSONObject): Position {
//            var displayLocation: Position = Position.FULL_SCREEN
//            try {
//                if (jsonObject.has(Companion.IAM_DISPLAY_LOCATION_KEY) && !jsonObject.get(Companion.IAM_DISPLAY_LOCATION_KEY).equals("")) displayLocation = Position.valueOf(
//                    jsonObject.optString(
//                        Companion.IAM_DISPLAY_LOCATION_KEY, "FULL_SCREEN"
//                    ).toUpperCase()
//                )
//            } catch (e: JSONException) {
//                e.printStackTrace()
//            }
//            return displayLocation
            return Position.CENTER_MODAL
        }

        private fun getDragToDismissDisabled(jsonObject: JSONObject): Boolean {
//            return try {
//                jsonObject.getBoolean(Companion.IAM_DRAG_TO_DISMISS_DISABLED_KEY)
//            } catch (e: JSONException) {
//                false
//            }
            return false
        }

        @Throws(JSONException::class)
        private fun handleActionTaken(jsonObject: JSONObject) {
//            val body: JSONObject = jsonObject.getJSONObject("body")
//            val id: String = body.optString("id", null)
//            closing = body.getBoolean("close")
//            if (message.isPreview) {
//                OneSignal.getInAppMessageController().onMessageActionOccurredOnPreview(message, body)
//            } else if (id != null) {
//                OneSignal.getInAppMessageController().onMessageActionOccurredOnMessage(message, body)
//            }
//            if (closing) {
//                dismissAndAwaitNextMessage(null)
//            }
        }

        @Throws(JSONException::class)
        private fun handlePageChange(jsonObject: JSONObject) {
//            OneSignal.getInAppMessageController().onPageChanged(message, jsonObject)
        }

        val JS_OBJ_NAME = "OSAndroid"
        val GET_PAGE_META_DATA_JS_FUNCTION = "getPageMetaData()"
        val SET_SAFE_AREA_INSETS_JS_FUNCTION = "setSafeAreaInsets(%s)"
        val SAFE_AREA_JS_OBJECT = "{\n" +
                "   top: %d,\n" +
                "   bottom: %d,\n" +
                "   right: %d,\n" +
                "   left: %d,\n" +
                "}"
        val SET_SAFE_AREA_INSETS_SCRIPT = "\n\n" +
                "<script>\n" +
                "    setSafeAreaInsets(%s);\n" +
                "</script>"
        val EVENT_TYPE_KEY = "type"
        val EVENT_TYPE_RENDERING_COMPLETE = "rendering_complete"
        val EVENT_TYPE_RESIZE = "resize"
        val EVENT_TYPE_ACTION_TAKEN = "action_taken"
        val EVENT_TYPE_PAGE_CHANGE = "page_change"
        val IAM_DISPLAY_LOCATION_KEY = "displayLocation"
        val IAM_PAGE_META_DATA_KEY = "pageMetaData"
        val IAM_DRAG_TO_DISMISS_DISABLED_KEY = "dragToDismissDisabled"
    }

    private val TAG = WebViewManager::class.java.canonicalName
    private val IN_APP_MESSAGE_INIT_DELAY = 200
    protected var lastInstance: WebViewManager? = null

    /**
     * Creates a new WebView
     * Dismiss WebView if already showing one and the new one is a Preview
     *
     * @param message the message to show
     * @param content the html to display on the WebView
     */
    fun showMessageContent(content: SurveyPresentation) {
//        val currentActivity: Activity = OneSignal.getCurrentActivity()
//        Logger.log(LogLevel.DEBUG, "in app message showMessageContent on currentActivity: $currentActivity")
//        /* IMPORTANT
//     * This is the starting route for grabbing the current Activity and passing it to InAppMessageView */if (currentActivity != null) {
//            // Only a preview will be dismissed, this prevents normal messages from being
//            // removed when a preview is sent into the app
//            if (lastInstance != null && message.isPreview) {
//                // Created a callback for dismissing a message and preparing the next one
//                lastInstance.dismissAndAwaitNextMessage(object : OneSignalGenericCallback {
//                    override fun onComplete() {
//                        lastInstance = null
//                        initInAppMessage(currentActivity, message, content)
//                    }
//                })
//            } else {
//                initInAppMessage(currentActivity, message, content)
//            }
//            return
//        }
//
//        /* IMPORTANT
//     * Loop the setup for in app message until curActivity is not null */
//        Looper.prepare()
//        Handler().postDelayed(Runnable { showMessageContent(message, content) }, IN_APP_MESSAGE_INIT_DELAY)
    }

    fun dismissCurrentInAppMessage() {
//        Logger.log(LogLevel.DEBUG, "WebViewManager IAM dismissAndAwaitNextMessage lastInstance: " + lastInstance)
//        if (lastInstance != null) {
//            lastInstance.dismissAndAwaitNextMessage(null)
//        }
    }

    private fun setContentSafeAreaInsets(content: SurveyPresentation, activity: Activity) {
//        var html: String? = content.getContentHtml()
//        var safeAreaInsetsScript = OSJavaScriptInterface.Companion.SET_SAFE_AREA_INSETS_SCRIPT
//        val insets: IntArray = ViewUtils.getCutoutAndStatusBarInsets(activity)
//        val safeAreaJSObject = String.format(OSJavaScriptInterface.Companion.SAFE_AREA_JS_OBJECT, insets[0], insets[1], insets[2], insets[3])
//        safeAreaInsetsScript = String.format(safeAreaInsetsScript, safeAreaJSObject)
//        html += safeAreaInsetsScript
//        content.setContentHtml(html)
    }

    private fun initInAppMessage(currentActivity: Activity, content: SurveyPresentation) {
//        if (content.isFullBleed) {
//            setContentSafeAreaInsets(content, currentActivity)
//        }
//        try {
//            val webViewManager = WebViewManager(currentActivity, content)
//            lastInstance = webViewManager
//
//            // Web view must be created on the main thread.
//            Utils.runOnMainUIThread(Runnable {
//                // Handles exception "MissingWebViewPackageException: Failed to load WebView provider: No WebView installed"
//                try {
//                    webViewManager.setupWebView(currentActivity, base64Str, content.isFullBleed())
//                } catch (e: Exception) {
//                    // Need to check error message to only catch MissingWebViewPackageException as it isn't public
//                    if (e.message != null && e.message!!.contains("No WebView installed")) {
//                        Logger.log(LogLevel.ERROR, "Error setting up WebView: ", e)
//                    } else {
//                        throw e
//                    }
//                }
//            })
//        } catch (e: UnsupportedEncodingException) {
//            Logger.log(LogLevel.ERROR, "Catch on initInAppMessage: ", e)
//            e.printStackTrace()
//        }
    }
}