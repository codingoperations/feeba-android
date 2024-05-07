package sample.project.project_list

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import io.feeba.Feeba
import io.least.demo.R
import io.least.demo.databinding.FragmentSampleShowcaseBinding
import sample.data.UserData
import sample.project.events.EventsAdapter
import sample.project.extractEvents
import sample.project.extractPageTriggers
import sample.project.integrated.InlineIntegratedSurvey
import sample.project.page.PageTriggerAdapter
import sample.project.page.PageType
import sample.project.prepareLogoutButton
import sample.utils.PreferenceWrapper

class ShowCaseFragment : Fragment() {

    private var _binding: FragmentSampleShowcaseBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val user1 = UserData(
        userId = "test1-user-id",
//        email = "test1@example.com",
//        phoneNumber = "+1-987-65-43",
//        tags = Tags(
//            rideId = "test1-user-ride-id",
//            driverId = "test1-driver-id"
//        )
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
        prepareLogoutButton(binding.logout, this)

        // Survey
        binding.dialogInView.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .addToBackStack("inview_survey")
                .replace(R.id.fragmentContainer, InlineIntegratedSurvey())
                .commit()
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

                val pageTriggers = extractPageTriggers(it)
                binding.recyclerViewPageTriggers.layoutManager = LinearLayoutManager(context)
                binding.recyclerViewPageTriggers.adapter = PageTriggerAdapter(pageTriggers) {
                    val bundle = Bundle().apply {
                        putString("page_name", it.event)
                    }
                    when(it.pageType) {
                        PageType.ACTIVITY -> findNavController().navigate(R.id.openActivityPageTrigger, bundle)
                        PageType.FRAGMENT -> findNavController().navigate(R.id.action_open_delayed_survey_fragment, bundle)
                    }
                }
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