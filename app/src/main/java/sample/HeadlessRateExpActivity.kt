package sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.least.core.ServerConfig
import io.least.core.collector.UserSpecificContext
import io.least.viewmodel.RateExperienceState
import io.least.viewmodel.RateExperienceViewModel
import io.sample.databinding.ActivityRateExpHeadlessBinding
import kotlinx.coroutines.flow.collect
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
    private val viewModel = RateExperienceViewModel(
        ServerConfig(
            hostUrl = "http://ec2-3-137-137-197.us-east-2.compute.amazonaws.com:8080",
            langCode = "ru",
            apiToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NjE4MDE5OTYsInBheWxvYWQiOnsidXNlcklkIjoiNjNkYzliMDFjN2IxNjYyZWE3MmQ3OTllIiwicHJvamVjdE5hbWUiOiJmZWViYSJ9LCJpYXQiOjE2NzU0MDE5OTZ9.VuIAQ90oQar5958nZHNKc6nOa8m3abzoFvGnk0cbpnY"
        ),
        usersContext = UserSpecificContext("info@feeba.io"),
    )

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
