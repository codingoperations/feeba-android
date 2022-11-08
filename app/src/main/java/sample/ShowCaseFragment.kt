package sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.least.ui.app.RateAppFragment
import io.least.ui.app.RateExpActivity
import io.least.viewmodel.RateAppConfig
import io.sample.R
import io.sample.databinding.FragmentSampleShowcaseBinding

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
            RateExpActivity.startActivity(requireActivity())
        }
        binding.buttonRateExperienceHeadless.setOnClickListener {
            HeadlessRateExpActivity.startActivity(requireActivity())
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}