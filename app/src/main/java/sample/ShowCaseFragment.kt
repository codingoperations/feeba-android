package sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import io.feeba.Feeba
import io.feeba.survey.KEY_SURVEY_URL
import io.feeba.survey.SurveyFragment
import io.least.core.ServerConfig
import io.least.core.collector.UserSpecificContext
import io.least.demo.databinding.FragmentSampleShowcaseBinding
import io.least.ui.app.RateAppFragment
import io.least.ui.app.RateExpActivity
import io.least.viewmodel.RateAppConfig
import kotlin.concurrent.fixedRateTimer

class ShowCaseFragment : Fragment() {

    private var _binding: FragmentSampleShowcaseBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSampleShowcaseBinding.inflate(inflater, container, false)
        binding.buttonRateApp.setOnClickListener {
            RateAppFragment.show(
                parentFragmentManager,
                requireActivity().classLoader,
                RateAppConfig(minPositiveRate = 3.0f)
            )
        }
        binding.buttonRateExperienceDefaultUi.setOnClickListener {
            RateExpActivity.startActivity(
                requireActivity(),
                ServerConfig(ConfigHolder.hostUrl, ConfigHolder.langCode, ConfigHolder.jwtToken),
                UserSpecificContext("info@feeba.io"),
                null,
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
            )
        }
        binding.buttonRateExperienceHeadless.setOnClickListener {
            HeadlessRateExpActivity.startActivity(requireActivity())
        }

        // Survey
        binding.surveyDialog.setOnClickListener {
            SurveyFragment()
                .apply {
                    arguments = Bundle().apply {
                        putString(
                            KEY_SURVEY_URL,
                            "http://dev-dashboard.feeba.io/s/feeba/6504ee57ba0d101292e066a8"
                        )
                    }
                }
                .show(
                    parentFragmentManager,
                    "SurveyFragment"
                )
        }
        binding.onCloseAction.setOnClickListener {
            Feeba.onEvent("on_ride_end")
        }
        binding.reportProblem.setOnClickListener {
            Feeba.onEvent("report_problem")
        }

        binding.switchEnv.setOnCheckedChangeListener { _, isChecked -> ConfigHolder.setEnv(isChecked) }
        ConfigHolder.langCode = binding.editTextLangCode.text.toString()
        binding.editTextLangCode.addTextChangedListener { text -> ConfigHolder.langCode = text.toString() }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}