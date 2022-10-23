package io.least.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.least.ServiceLocator
import io.least.core.ServerConfig
import io.least.core.collector.DeviceDataCollector
import io.least.core.collector.UserSpecificContext
import io.least.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RateExperienceViewModel : ViewModel {

    // Backing property to avoid state updates from other classes
    private val _uiState = MutableStateFlow<RateExperienceState>(RateExperienceState.ConfigLoading)

    // The UI collects from this StateFlow to get its state updates
    val uiState: StateFlow<RateExperienceState> = _uiState

    private var config: RateExperienceConfig? = null
    private val repository: RateExperienceRepository
    private val usersContext: UserSpecificContext

    constructor(
        config: RateExperienceConfig,
        serverConfig: ServerConfig,
        usersContext: UserSpecificContext,
        repository: RateExperienceRepository = RateExperienceRepository(
            ServiceLocator.getHttpClient(serverConfig), DeviceDataCollector(), serverConfig
        )
    ) {
        this.config = config
        this.usersContext = usersContext
        this.repository = repository
        _uiState.value = RateExperienceState.ConfigLoaded(config)
    }

    constructor(
        serverConfig: ServerConfig,
        usersContext: UserSpecificContext,
        repository: RateExperienceRepository = RateExperienceRepository(
            ServiceLocator.getHttpClient(serverConfig), DeviceDataCollector(), serverConfig
        )
    ) {
        this.usersContext = usersContext
        this.repository = repository
        _uiState.value = RateExperienceState.ConfigLoading

        viewModelScope.launch {
            kotlin.runCatching { repository.fetchRateExperienceConfig() }
                .onSuccess {
                    _uiState.value = RateExperienceState.ConfigLoaded(it)
                    config = it
                }
                .onFailure {
                    Log.e(
                        this.javaClass.simpleName,
                        "Failed to fetch config: ${Log.getStackTraceString(it)}"
                    )
                    _uiState.value = RateExperienceState.ConfigLoadFailed
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
