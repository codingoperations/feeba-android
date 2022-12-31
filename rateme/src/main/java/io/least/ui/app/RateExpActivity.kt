package io.least.ui.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import io.least.core.collector.UserSpecificContext
import io.least.core.ServerConfig
import io.least.data.LabelValue
import io.least.data.RateExperienceConfig
import io.least.data.Tag
import io.least.rate.R
import io.least.rate.databinding.ActivityRatemeExpBinding
import io.least.ui.experience.RateExperienceFragment

class RateExpActivity : AppCompatActivity() {
    private var _binding: ActivityRatemeExpBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRatemeExpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        RateExperienceFragment.show(
            supportFragmentManager,
            R.id.fragmentContainer,
            this.classLoader,
//            RateExperienceConfig(
//                tags = listOf(Tag("id1", "tag1"), Tag("id2", "tag2"), Tag("id3", "tag3")),
//                numberOfStars = 6,
//                valueReaction = listOf(
//                    LabelValue(1, "too bad :("),
//                    LabelValue(2, "Nice ;)"),
//                    LabelValue(8, "Great!")
//                ),
//                title = "MY TITLE",
//                postSubmitTitle = "It is post submit Title",
//                postSubmitText = "It is post submit BODY TEXT",
//            ),
            null,
            ServerConfig(
                hostUrl = "https://cf43-2601-601-1800-750-2125-3f1f-48be-74e4.ngrok.io",
                langCode = "ru",
                apiToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NTg1MjYwMTcsInBheWxvYWQiOnsidXNlcklkIjoiNjM4YzE3YjQ1ZGE2ZDE4YWIwYjc1MWQ0IiwicHJvamVjdE5hbWUiOiJ0ZXN0In0sImlhdCI6MTY3MjEyNjAxN30.KvBEL_FHMY8xU7Uj6TLfEpkuZrnadtb1eFZevP5m0ho"
            ),
            withBackStack = false,
            usersContext = UserSpecificContext("info@feeba.io"),
//                View.inflate(requireContext(), R.layout.c, null)
        )
    }

    companion object {
        @JvmStatic
        fun startActivity(activity: Activity, ) {
            activity.startActivity(Intent(activity, RateExpActivity::class.java))
        }
    }
}
