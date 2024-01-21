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

class ShowCaseFragment : Fragment() {

    private var _binding: FragmentSampleShowcaseBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Feeba.User.login("test_user_id", "admin@google.com", "+1-123-456-7890")
        Feeba.User.setLanguage("en")
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSampleShowcaseBinding.inflate(inflater, container, false)

        // Survey
        binding.dialogInView.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .addToBackStack("inview_survey")
                .replace(R.id.fragmentContainer, InViewIntegratedSurvey())
                .commit()
        }

        binding.onRideEndButton.setOnClickListener {
            Feeba.User.addTag(mapOf("driverId" to "1234", "rideId" to "5678"))
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

        binding.switchEnv.setOnCheckedChangeListener { _, isChecked -> ConfigHolder.setEnv(isChecked) }
        ConfigHolder.langCode = binding.editTextLangCode.text.toString()
        binding.editTextLangCode.addTextChangedListener { text -> ConfigHolder.langCode = text.toString() }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Feeba.User.logout()
        _binding = null
    }
}