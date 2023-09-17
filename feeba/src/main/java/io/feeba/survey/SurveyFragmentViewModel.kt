package io.feeba.survey

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.least.core.ServerConfig
import io.least.core.collector.DeviceDataCollector
import io.least.data.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SurveyFragmentViewModel(application: Application, serverConfig: ServerConfig, val surveyUrl: String) : AndroidViewModel(application) {

    // Backing property to avoid state updates from other classes
    private val _uiState = MutableStateFlow<SurveyFragmentState>(SurveyFragmentState.SurveyLoading)

    // The UI collects from this StateFlow to get its state updates
    val uiState: StateFlow<SurveyFragmentState> = _uiState
    private val repository: SurveyRepository
    private val TAG = this.javaClass.simpleName

    init {
        this.repository = SurveyRepository(
            application, HttpClient(serverConfig), DeviceDataCollector(), serverConfig
        )
        _uiState.value = SurveyFragmentState.SurveyLoading
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) { runCatching { repository.fetchSurvey(surveyId) } }
//                .onSuccess {
//                    _uiState.value = SurveyFragmentState.SurveyLoaded(it)
//                }
//                .onFailure {
//                    Log.e(TAG, "Failed to fetch config: ${Log.getStackTraceString(it)}")
//                    _uiState.value = SurveyFragmentState.SurveyLoadFailed()
//                }
//        }
    }
}

sealed class SurveyFragmentState {
    object SurveyLoading : SurveyFragmentState()
    class SurveyLoaded(val surveyUrl: String) : SurveyFragmentState()
    class SurveyLoadFailed : SurveyFragmentState()
}
