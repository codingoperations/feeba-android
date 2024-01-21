package sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import io.feeba.Feeba
import io.least.demo.R
import io.least.demo.databinding.FragmentSampleShowcaseBinding
import sample.bugs.ProblematicLoginPage
import sample.integrated.InViewIntegratedSurvey
import sample.utils.PreferenceWrapper

class ShowCaseFragment : Fragment() {

    private var _binding: FragmentSampleShowcaseBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val user1 = UserData(
        userId = "test1-user-id",
        email = "test1@example.com",
        phoneNumber = "+1-987-65-43",
        tags = Tags(
            rideId = "test1-user-ride-id",
            driverId = "test1-driver-id"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Feeba.User.login(user1.userId, user1.email, user1.phoneNumber)
        Feeba.User.setLanguage("en")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSampleShowcaseBinding.inflate(inflater, container, false)
        binding.switchEnv.isChecked = PreferenceWrapper.isProd
        binding.editTextLangCode.setText(PreferenceWrapper.langCode)

        // Survey
        binding.dialogInView.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .addToBackStack("inview_survey")
                .replace(R.id.fragmentContainer, InViewIntegratedSurvey())
                .commit()
        }

        binding.onRideEndButton.setOnClickListener {
            Feeba.User.addTag(mapOf("driverId" to user1.tags.driverId, "rideId" to user1.tags.rideId))
            Feeba.triggerEvent("on_ride_end")
        }
        // BUGs and issue reporting
        binding.reportProblem.setOnClickListener {
            Feeba.triggerEvent("report_problem")
        }

        binding.buttonHavingTrouble.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .addToBackStack("login_page")
                .replace(R.id.fragmentContainer, ProblematicLoginPage())
                .commit()
        }
        binding.switchEnv.setOnCheckedChangeListener { _, isChecked ->
            ConfigHolder.setEnv(isChecked)
            PreferenceWrapper.isProd = isChecked
        }
        binding.editTextLangCode.addTextChangedListener { text ->
            Feeba.User.setLanguage(text.toString())
            PreferenceWrapper.langCode = text.toString()
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Feeba.User.logout()
        _binding = null
    }
}