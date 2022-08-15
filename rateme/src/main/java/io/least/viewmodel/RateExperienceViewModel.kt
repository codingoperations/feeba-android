package io.least.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.least.core.collector.UserSpecificContext
import io.least.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RateExperienceViewModel(
    @Volatile private var config: RateExperienceConfig?,
    private val repository: RateExperienceRepository,
    private val usersContext: UserSpecificContext,
) : ViewModel() {

    // Backing property to avoid state updates from other classes
    private val _uiState = MutableStateFlow<RateExperienceState>(RateExperienceState.ConfigLoading)

    // The UI collects from this StateFlow to get its state updates
    val uiState: StateFlow<RateExperienceState> = _uiState

    init {
        config?.let {
            _uiState.value = RateExperienceState.ConfigLoaded(it)
        } ?: kotlin.run {
            _uiState.value = RateExperienceState.ConfigLoading
            viewModelScope.launch {
                kotlin.runCatching { repository.fetchRateExperienceConfig() }
                    .onSuccess {
                        _uiState.value = RateExperienceState.ConfigLoaded(it)
                        config = it
                    }
                    .onFailure {
                        Log.e(this.javaClass.simpleName,"Failed to fetch config: ${Log.getStackTraceString(it)}")
                        _uiState.value = RateExperienceState.ConfigLoadFailed
                    }
            }
        }
    }

    fun onFeedbackSubmit(text: String, rating: Float, selectedTags: List<Tag>) {
        Log.d(this.javaClass.simpleName, "Creating a case --> $text")
        _uiState.value = RateExperienceState.Submitting
        config?.let { rateExpConfig ->
            viewModelScope.launch {
                try {
                    repository.publishRateResults(
                        RateExperienceResult(
                            selectedTags,
                            rating.toInt(),
                            rateExpConfig.numberOfStars,
                            text,
                            usersContext
                        )
                    )
                    _uiState.value = RateExperienceState.SubmissionSuccess(rateExpConfig)
                } catch (t: Throwable) {
                    Log.e(this.javaClass.simpleName, Log.getStackTraceString(t))

                    _uiState.value = RateExperienceState.SubmissionError
                }
            }
        }
    }

    fun onRateSelected(rating: Float) {
        config?.let {
            for (it in it.valueReactions) {
                if (rating.toInt() <= it.value) {
                    _uiState.value = RateExperienceState.RateSelected(it.label)
                    break
                }
            }
        }
    }
}

sealed class RateExperienceState {
    object ConfigLoading : RateExperienceState()
    class RateSelected(val reaction: String) : RateExperienceState()
    class ConfigLoaded(val config: RateExperienceConfig) : RateExperienceState()
    object ConfigLoadFailed : RateExperienceState()
    object Submitting : RateExperienceState()
    object SubmissionError : RateExperienceState()
    class SubmissionSuccess(val config: RateExperienceConfig) : RateExperienceState()
}
