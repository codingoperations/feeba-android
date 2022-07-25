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
                hostUrl = "https://least-service.herokuapp.com/",
                apiToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NDQ2ODQ4MjYsInBheWxvYWQiOnsidXNlcklkIjoiNjJjMjU2NzE1ZjRiZjJlODE5N2Q0NTRmIiwicHJvamVjdE5hbWUiOiJhYSJ9LCJpYXQiOjE2NTgyODQ4MjZ9.pzAu6pYWaW7D7STzE6q55NJHOwSs_GorgunNSfUal0E"
            ),
            withBackStack = false,
            usersContext = UserSpecificContext("bkodirov1986@gmail.com"),
//                View.inflate(requireContext(), R.layout.c, null)
        )
    }

    companion object {
        @JvmStatic
        fun startActivity(activity: Activity) {
            activity.startActivity(Intent(activity, RateExpActivity::class.java))
        }
    }
}
