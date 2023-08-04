package io.least.ui.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.least.core.ServerConfig
import io.least.core.collector.UserSpecificContext
import io.least.data.RateExperienceConfig
import io.feeba.R
import io.feeba.databinding.ActivityRatemeExpBinding
import io.least.ui.experience.RateExperienceFragment

const val KEY_SERVER_CONFIG = "server-config"
const val KEY_USER_CONTEXT = "user-context"
const val KEY_RATE_EXP_CONFIG = "rateexp-config"
class RateExpActivity : AppCompatActivity() {
    private var _binding: ActivityRatemeExpBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRatemeExpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val serverConfig = intent.getSerializableExtra(KEY_SERVER_CONFIG) as ServerConfig
        val userContext = intent.getSerializableExtra(KEY_USER_CONTEXT) as UserSpecificContext
        val rateExperienceConfig = intent.getSerializableExtra(KEY_RATE_EXP_CONFIG) as? RateExperienceConfig

        RateExperienceFragment.show(
            supportFragmentManager,
            R.id.fragmentContainer,
            this.classLoader,
            rateExperienceConfig,
            serverConfig,
            withBackStack = false,
            userContext
//                View.inflate(requireContext(), R.layout.c, null)
        )
    }

    companion object {
        @JvmStatic
        fun startActivity(
            activity: Activity,
            serverConfig: ServerConfig,
            userContext: UserSpecificContext,
            rateExperienceConfig: RateExperienceConfig?
        ) {
            val intent = Intent(activity, RateExpActivity::class.java)
            intent.putExtra(KEY_SERVER_CONFIG, serverConfig)
            intent.putExtra(KEY_RATE_EXP_CONFIG, rateExperienceConfig)
            intent.putExtra(KEY_USER_CONTEXT, userContext)
            activity.startActivity(intent)
        }
    }
}
