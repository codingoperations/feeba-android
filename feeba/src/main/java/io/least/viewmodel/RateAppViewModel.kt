package io.least.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RateMeViewModel (
    private val config: RateAppConfig,
//    private val connector: Connector<String>?
): ViewModel() {

    // Backing property to avoid state updates from other classes
    private val _uiState = MutableStateFlow<RateMeUiState>(RateMeUiState.Initial)

    // The UI collects from this StateFlow to get its state updates
    val uiState: StateFlow<RateMeUiState> = _uiState

    fun onFeedbackSubmit(text: String) {
//        connector?.create(text)
    }
    fun onNotNow() {}
    fun onNeverAskAgain() {}

    fun onRated(rating: Float, activity: Activity) {
        if (rating < config.minPositiveRate) {
            _uiState.value = RateMeUiState.NegativeSubmitted(rating)
        } else {
            // We don't have to ask user to open the Review dialog if the Playmarket is installed
//            if (isPlayServiceAvailable) {
            // TODO Open ReviewManager conditionally. Provide an option to open other different apps like Yandex Market ect
            val manager = ReviewManagerFactory.create(activity)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                Log.d("RateMeViewModel", "addOnCompleteListener")
                if (task.isSuccessful) {
                    Log.d("RateMeViewModel", "task.isSuccessful")
                    // We got the ReviewInfo object
                    val reviewInfo = task.result
                    val flow = manager.launchReviewFlow(activity, reviewInfo)
                    flow.addOnCompleteListener { _ ->
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even whether the review dialog was shown. Thus, no
                        // matter the result, we continue our app flow.
                        // TODO Log locally and remotely to identify the Use case end
                        Log.d("RateMeViewModel", "flow.addOnCompleteListener")
                    }
                } else {
                    // There was some problem, log or handle the error code.
//                    @ReviewErrorCode val reviewErrorCode = (task.exception as TaskException).errorCode
                    Log.e("RateMeViewModel", task.exception?.toString() ?: "dummy text")
                }
            }
//            } else if(yandexMarket) {
//
//            } else if (huaweiMarket) {
//
//            } else {
//                // TODO
//            }
        }
    }
}

sealed class RateMeUiState {
    object Initial : RateMeUiState()
    data class NegativeSubmitted(val rate: Float) : RateMeUiState()
    object PositiveSubmitted : RateMeUiState()
}
