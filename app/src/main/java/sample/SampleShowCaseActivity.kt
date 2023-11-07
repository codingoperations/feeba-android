package sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.feeba.Feeba
import io.least.core.ServerConfig
import io.least.demo.R
import io.least.demo.databinding.ActivitySampleShowcaseBinding

class SampleShowCaseActivity : AppCompatActivity() {
    private var _binding: ActivitySampleShowcaseBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ConfigHolder.setEnv(false)
        Feeba.init(
            this.application, ServerConfig(
                hostUrl = ConfigHolder.hostUrl,
                langCode = ConfigHolder.langCode,
                apiToken = ConfigHolder.jwtToken
            )
        )
        _binding = ActivitySampleShowcaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, ShowCaseFragment())
            .commit()
    }
}