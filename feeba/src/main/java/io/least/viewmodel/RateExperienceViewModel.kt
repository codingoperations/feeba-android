package io.least.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.least.core.ServerConfig
import io.least.core.collector.DeviceDataCollector
import io.least.core.collector.UserSpecificContext
import io.least.data.HttpClient
import io.least.data.RateExperienceConfig
import io.least.data.RateExperienceRepository
import io.least.data.RateExperienceResult
import io.least.data.Tag
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

class RateExperienceViewModel : AndroidViewModel {

    // That last known rating from the user
    private var lastKnownRating: Int = 0

    // Backing property to avoid state updates from other classes
    private val _uiState = MutableStateFlow<RateExperienceState>(RateExperienceState.ConfigLoading)

    // The UI collects from this StateFlow to get its state updates
    val uiState: StateFlow<RateExperienceState> = _uiState

    private var config: RateExperienceConfig? = null
    private val repository: RateExperienceRepository
    var usersContext: UserSpecificContext
    private val tagSelectionHistory = LinkedHashMap<String, Tag>()
    private val TAG = this.javaClass.simpleName

    constructor(
        application: Application,
        config: RateExperienceConfig,
        serverConfig: ServerConfig,
        usersContext: UserSpecificContext,
    ) : super(application) {
        this.config = config
        this.usersContext = usersContext
        this.repository = RateExperienceRepository(
            application, HttpClient(serverConfig), DeviceDataCollector(), serverConfig
        )
        _uiState.value = RateExperienceState.ConfigLoaded(config)
    }

    constructor(
        application: Application,
        serverConfig: ServerConfig,
        usersContext: UserSpecificContext,
    ) : super(application) {
        this.usersContext = usersContext
        this.repository = RateExperienceRepository(
            application, HttpClient(serverConfig), DeviceDataCollector(), serverConfig
        )
        _uiState.value = RateExperienceState.ConfigLoading

        viewModelScope.launch {
            withContext(Dispatchers.IO) { runCatching { repository.fetchRateExperienceConfig() } }
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
        config?.let { rateExpConfig ->
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch {
                repository.publishRateResults(
                    RateExperienceResult(
                        tagSelectionHistory.values.toList(),
                        rating.toInt(),
                        rateExpConfig.numberOfStars,
                        text,
                        usersContext
                    )
                )
            }
            _uiState.value = RateExperienceState.SubmissionSuccess(rateExpConfig)
        }
    }

    /**
     * THis function needs to be called when the user selects a rating. If the rating is changed backend
     * will potentially will send new set of Tags that are related to  the rate user selected.
     * If the Tags are changed, the UI RateExperienceState.TagsUpdated will be emitted with the new set of Tags.
     */
    fun onRateSelected(rating: Int) {
        config?.let { localConfig ->
            for (it in localConfig.valueReactions) {
                if (rating <= it.value) {
                    _uiState.value = RateExperienceState.RateSelected(it.label)
                    break
                }
            }
            // Don't fet tags if the rating is the same(Negative or Positive)
            if (
                lastKnownRating == 0 // If this a first time selection
                ||
                (lastKnownRating < localConfig.minPositiveRate && rating >= localConfig.minPositiveRate)
                ||
                (lastKnownRating >= localConfig.minPositiveRate && rating < localConfig.minPositiveRate)
            ) {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val tags = repository.fetchTags(rating)
                        config = localConfig.copy(tags = tags.tags)
                        tagSelectionHistory.clear()
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

    fun onTagSelectionUpdate(tag: Tag, isChecked: Boolean) {
        Log.d("onTagSelectionUpdate", tag.text + " " + isChecked)
        config?.also {
            if (isChecked) {
                tagSelectionHistory[tag.id] = tag
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

    @Deprecated("This will be removed in the future. Use SubmissionSuccess instead")
    object Submitting : RateExperienceState()

    @Deprecated("This will be removed in the future. Use SubmissionSuccess instead")
    object SubmissionError : RateExperienceState()
    class SubmissionSuccess(val config: RateExperienceConfig) : RateExperienceState()
}
