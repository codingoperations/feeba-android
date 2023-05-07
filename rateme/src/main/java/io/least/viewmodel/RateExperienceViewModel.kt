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
import kotlinx.serialization.Serializable

class RateExperienceViewModel : ViewModel {

    // That last known rating from the user
    private var lastKnownRating: Int = 0

    // Backing property to avoid state updates from other classes
    private val _uiState = MutableStateFlow<RateExperienceState>(RateExperienceState.ConfigLoading)

    // The UI collects from this StateFlow to get its state updates
    val uiState: StateFlow<RateExperienceState> = _uiState

    private var config: RateExperienceConfig? = null
    private val repository: RateExperienceRepository
    private val usersContext: UserSpecificContext
    private val tagSelectionHistory = LinkedHashMap<String, Tag>()
    private val TAG = this.javaClass.simpleName

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
                    Log.e(TAG, "Failed to fetch config: ${Log.getStackTraceString(it)}")
                    _uiState.value = RateExperienceState.ConfigLoadFailed
                }
        }
    }

    fun onFeedbackSubmit(text: String, rating: Float) {
        Log.d(TAG, "Creating a case --> $text")
        _uiState.value = RateExperienceState.Submitting
        config?.let { rateExpConfig ->
            viewModelScope.launch {
                try {
                    repository.publishRateResults(
                        RateExperienceResult(
                            tagSelectionHistory.values.toList(),
                            rating.toInt(),
                            rateExpConfig.numberOfStars,
                            text,
                            usersContext
                        )
                    )
                    _uiState.value = RateExperienceState.SubmissionSuccess(rateExpConfig)
                } catch (t: Throwable) {
                    Log.e(TAG, Log.getStackTraceString(t))

                    _uiState.value = RateExperienceState.SubmissionError
                }
            }
        }
    }

    fun onRateSelected(rating: Int) {
        config?.let {
            for (it in it.valueReactions) {
                if (rating.toInt() <= it.value) {
                    _uiState.value = RateExperienceState.RateSelected(it.label)
                    break
                }
            }
            // Don't fet tags if the rating is the same(Negative or Positive)
            if (
                (lastKnownRating < it.firstPositiveRate && rating >= it.firstPositiveRate)
                ||
                (lastKnownRating >= it.firstPositiveRate && rating < it.firstPositiveRate)
            ) {
                viewModelScope.launch {
                    try {
                        val tags = repository.fetchTags(rating)
                        _uiState.value = RateExperienceState.TagsUpdated(
                            tags.tags,
                            tagSelectionHistory.values.toList()
                        )
                    } catch (t: Throwable) {
                        Log.e(TAG, Log.getStackTraceString(t))
                    }
                }
            }
        }
        this.lastKnownRating = rating
    }

    /**
     * The function to be called when user clicks on Tags. ViewModel might update tags dynamically based on the selection.
     * The effect of the function is observed through UI State flow, reactive back channel.
     */
    fun onTagSelectionUpdate(tag: Tag, isChecked: Boolean) {
        config?.also {
            if (isChecked) {
                tagSelectionHistory[tag.id] = tag
//                if (it.isPremium == true) {
//                    viewModelScope.launch {
//                        try {
//                            val tagUpdate = repository.fetchTags(TagUpdate(it.tags)
//                            ) ?: return@launch
//                            config = it.copy(tags = tagUpdate.tags)

//                        } catch (t: Throwable) {
//                            Log.e(TAG, Log.getStackTraceString(t))
//                        }
//                    }
//                }
            } else {
                tagSelectionHistory.remove(tag.id)
            }
            _uiState.value = RateExperienceState.TagsUpdated(it.tags, tagSelectionHistory.values.toList())
        } ?: kotlin.run {
            Log.w(TAG, "Config is not available yet")
        }
    }
}

sealed class RateExperienceState {
    object ConfigLoading : RateExperienceState()
    class RateSelected(val reaction: String) : RateExperienceState()

    @Serializable
    class ConfigLoaded(val config: RateExperienceConfig) : RateExperienceState()
    class TagsUpdated(val tags: List<Tag>, val selectionHistory: List<Tag>) : RateExperienceState()
    object ConfigLoadFailed : RateExperienceState()
    object Submitting : RateExperienceState()
    object SubmissionError : RateExperienceState()
    class SubmissionSuccess(val config: RateExperienceConfig) : RateExperienceState()
}
