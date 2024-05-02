package sample.project.project_list

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import io.feeba.Feeba
import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger
import io.least.demo.R
import io.least.demo.databinding.FragmentSampleShowcaseBinding
import sample.ConfigHolder
import sample.data.Tags
import sample.data.UserData
import sample.project.page.PageTriggerActivity
import sample.project.page.bugs.ProblematicLoginPage
import sample.project.events.EventsAdapter
import sample.project.extractEvents
import sample.project.integrated.InlineIntegratedSurvey
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
        binding.editTextLangCode.setText(PreferenceWrapper.langCode)

        binding.logout.setOnClickListener {
            Feeba.User.logout()
            PreferenceWrapper.jwtToken = ""
            // pop back to the upmost fragment
            findNavController().popBackStack(R.id.fragmentLogin, false)
        }
        // Survey
        binding.dialogInView.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .addToBackStack("inview_survey")
                .replace(R.id.fragmentContainer, InlineIntegratedSurvey())
                .commit()
        }

        // Page Triggers
        binding.buttonPageTriggerFragment.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .addToBackStack("login_page")
                .replace(R.id.fragmentContainer, ProblematicLoginPage())
                .commit()
        }

        binding.buttonPageTriggerActivity.setOnClickListener {
            startActivity(Intent(requireContext(), PageTriggerActivity::class.java))
        }
        // End of Page Triggers
        binding.editTextLangCode.addTextChangedListener { text ->
            Feeba.User.setLanguage(text.toString())
            PreferenceWrapper.langCode = text.toString()
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Feeba.onConfigUpdate {
            Handler(Looper.getMainLooper()).post {
                val adapterData = extractEvents(it)
                binding.recyclerViewEventTriggers.layoutManager = LinearLayoutManager(context)
                binding.recyclerViewEventTriggers.adapter = EventsAdapter(adapterData)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Feeba.onConfigUpdate { null }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Feeba.User.logout()
        _binding = null
    }
}