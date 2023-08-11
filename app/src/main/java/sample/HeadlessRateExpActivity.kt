package sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import io.least.core.ServerConfig
import io.least.core.collector.UserSpecificContext
import io.least.core.createWithFactory
import io.least.demo.databinding.ActivityRateExpHeadlessBinding
import io.least.viewmodel.RateExperienceState
import io.least.viewmodel.RateExperienceViewModel
import io.least.viewmodel.RateMeViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class HeadlessRateExpActivity : AppCompatActivity() {
    private var _binding: ActivityRateExpHeadlessBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    /**
     * View model is initialized here. All the config is passed as a part of the ViewModel initialization
     * based on what lang code is passed, backend returns the correlated config
     */
    private val viewModel by viewModels<RateExperienceViewModel> {
        createWithFactory {
            RateExperienceViewModel(
                this.application,
                ServerConfig(
                    hostUrl = ConfigHolder.hostUrl,
                    langCode = ConfigHolder.langCode,
                    apiToken = ConfigHolder.jwtToken
                ),
                usersContext = UserSpecificContext("info@feeba.io"),
            )
        }
    }


    /**
     * Once the onCreate is invoked we start listening to changes from ViewModel
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRateExpHeadlessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                Log.d(this.javaClass.simpleName, "uiState.collect -> $uiState")
                // New value received
                when (uiState) {
                    is RateExperienceState.ConfigLoaded -> {
                        binding.textView.text = Json.encodeToString(uiState)
                    }

                    RateExperienceState.ConfigLoading -> {
                        binding.textView.text = "Loading"
                    }

                    RateExperienceState.ConfigLoadFailed -> {
                        binding.textView.text = "Loading Failed"
                    }

                    else -> {
                        binding.textView.text = uiState.toString()
                    }
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun startActivity(activity: Activity) {
            activity.startActivity(Intent(activity, HeadlessRateExpActivity::class.java))
        }
    }
}
