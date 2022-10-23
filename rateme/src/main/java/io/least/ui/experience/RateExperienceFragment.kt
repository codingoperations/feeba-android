package io.least.ui.experience

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.chip.Chip
import io.least.ServiceLocator
import io.least.core.collector.DeviceDataCollector
import io.least.core.collector.UserSpecificContext
import io.least.core.ServerConfig
import io.least.core.createWithFactory
import io.least.data.*
import io.least.rate.R
import io.least.rate.databinding.RateExpFragmentBinding
import io.least.viewmodel.RateExperienceState
import io.least.viewmodel.RateExperienceViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class RateExperienceFragment(
    private val rateExperienceConfig: RateExperienceConfig?,
    private val serverConfig: ServerConfig,
    private val usersContext: UserSpecificContext,
    private val customView: View?,
) : Fragment() {

    companion object {

        fun show(
            supportFragmentManager: FragmentManager,
            @IdRes containerId: Int,
            classLoader: ClassLoader,
            rateExperienceConfig: RateExperienceConfig?,
            serverConfig: ServerConfig,
            withBackStack: Boolean,
            usersContext: UserSpecificContext,
            customView: View? = null,
        ) {
            supportFragmentManager.fragmentFactory = RateExperienceFragmentFactory(
                rateExperienceConfig,
                serverConfig,
                usersContext,
                customView
            )
            val fragment = supportFragmentManager.fragmentFactory.instantiate(
                classLoader,
                RateExperienceFragment::class.java.name
            )
            supportFragmentManager.beginTransaction()
                .add(containerId, fragment)
                .apply { if (withBackStack) addToBackStack(RateExperienceFragment::class.java.simpleName) }
                .commit()
        }
    }

    private var _binding: RateExpFragmentBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: RateExperienceViewModel by viewModels {
        createWithFactory {
            rateExperienceConfig?.let { RateExperienceViewModel(it, serverConfig, usersContext) }
                ?: kotlin.run { RateExperienceViewModel(serverConfig, usersContext) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Start a coroutine in the lifecycle scope
        viewLifecycleOwner.lifecycleScope.launch {
            // repeatOnLifecycle launches the block in a new coroutine every time the
            // lifecycle is in the STARTED state (or above) and cancels it when it's STOPPED.
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Trigger the flow and start listening for values.
                // Note that this happens when lifecycle is STARTED and stops
                // collecting when the lifecycle is STOPPED
                viewModel.uiState.collect { uiState ->
                    Log.d(this.javaClass.simpleName, "uiState.collect -> $uiState")
                    // New value received
                    when (uiState) {
                        is RateExperienceState.ConfigLoaded -> {
                            binding.groupLoading.visibility = View.GONE
                            binding.groupLoaded.visibility = View.VISIBLE
                            binding.groupFinalLayout.visibility = View.GONE
                            populateView(uiState.config)
                        }
                        RateExperienceState.ConfigLoading -> {
                            binding.groupLoading.visibility = View.VISIBLE
                            binding.groupLoaded.visibility = View.GONE
                            binding.groupFinalLayout.visibility = View.GONE
                        }
                        is RateExperienceState.RateSelected -> {
                            binding.textViewReaction.text = uiState.reaction
                        }
                        RateExperienceState.SubmissionError -> {
                            binding.buttonSubmit.text = getString(R.string.rate_exp_retry)
                            binding.buttonSubmit.isEnabled = true
                        }
                        is RateExperienceState.SubmissionSuccess -> {
                            binding.groupLoading.visibility = View.GONE
                            binding.groupLoaded.visibility = View.GONE
                            binding.groupFinalLayout.visibility = View.VISIBLE
                            binding.finalRatingBar.numStars = binding.ratingBar.numStars
                            binding.finalRatingBar.rating = binding.ratingBar.rating
                            binding.finalGratitudeText.text = uiState.config.postSubmitText
                            activity?.title = uiState.config.postSubmitTitle
                        }
                        RateExperienceState.Submitting -> {
                            binding.buttonSubmit.text = getString(R.string.rate_exp_submitted)
                            binding.buttonSubmit.isEnabled = false
                        }
                        RateExperienceState.ConfigLoadFailed -> {
                            activity?.finish()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RateExpFragmentBinding.inflate(inflater, container, false)
        binding.buttonSubmit.setOnClickListener {
            viewModel.onFeedbackSubmit(
                binding.editFeedback.text.toString(),
                binding.ratingBar.rating,
                binding.tagGroup.checkedChipIds.map {
                    val chipView: Chip = binding.tagGroup.findViewById(it)
                    chipView.tag as Tag
                }
            )
        }
        customView?.let { binding.customViewHolder.addView(it) }
        return binding.root
    }

    private fun populateView(config: RateExperienceConfig) {
        binding.textViewHeader.text = config.title
        binding.ratingBar.numStars = config.numberOfStars
        binding.ratingBar.stepSize = 1f
        binding.tagGroup.removeAllViews()
        config.tags.forEach { tag ->
            val chip =
                layoutInflater.inflate(R.layout.layout_single_chip, binding.tagGroup, false) as Chip
            chip.apply {
                text = tag.text
                this.tag = tag
                isCheckable = true
                isClickable = true
                isFocusable = true
            }
            binding.tagGroup.addView(chip)
        }
        binding.ratingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (fromUser) {
                viewModel.onRateSelected(rating)
            }
        }
        binding.finalGratitudeText.text = config.postSubmitText
        activity?.title = config.title
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}