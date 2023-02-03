package io.least.ui.app

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import io.least.core.createWithFactory
import io.least.rate.R
import io.least.rate.databinding.RateAppFragmentBinding
import io.least.ui.showKeyboard
import io.least.viewmodel.RateAppConfig
import io.least.viewmodel.RateMeUiState
import io.least.viewmodel.RateMeViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


private const val TAG = "RateMeFragment"

class RateAppFragment(
    private val config: RateAppConfig,
//    private val connector: Connector<String>?
) : DialogFragment() {

    companion object {

        fun show(
            supportFragmentManager: FragmentManager,
            classLoader: ClassLoader,
//            connector: Connector<String>?,
            config: RateAppConfig
        ) {
            supportFragmentManager.fragmentFactory = RatemeFragmentFactory(
                config,
//                connector
            )
            val fragment: DialogFragment = supportFragmentManager.fragmentFactory.instantiate(
                classLoader,
                RateAppFragment::class.java.name
            ) as DialogFragment
            fragment.show(supportFragmentManager, TAG)
        }
    }

    private var _binding: RateAppFragmentBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: RateMeViewModel by viewModels {
        createWithFactory {
            RateMeViewModel(
                config,
//                connector
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Start a coroutine in the lifecycle scope
        lifecycleScope.launch {
            // repeatOnLifecycle launches the block in a new coroutine every time the
            // lifecycle is in the STARTED state (or above) and cancels it when it's STOPPED.
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Trigger the flow and start listening for values.
                // Note that this happens when lifecycle is STARTED and stops
                // collecting when the lifecycle is STOPPED
                viewModel.uiState.collect { uiState ->
                    // New value received
                    when (uiState) {
                        is RateMeUiState.Initial -> {
                            binding.textViewHeader.text = getText(R.string.rate_header_initial)
                        }
                        is RateMeUiState.NegativeSubmitted -> {
                            binding.groupStageAsk.visibility = View.GONE
                            binding.groupStageNegativeFeedback.visibility = View.VISIBLE
                            binding.groupStagePositiveFeedback.visibility = View.GONE
                            binding.textViewHeader.text =
                                getText(R.string.rate_header_negative_submitted)
                            binding.editFeedback.requestFocus()
                            showKeyboard(binding.editFeedback)
                        }
                        is RateMeUiState.PositiveSubmitted -> {
                            binding.groupStageAsk.visibility = View.GONE
                            binding.groupStageNegativeFeedback.visibility = View.GONE
                            binding.groupStagePositiveFeedback.visibility = View.VISIBLE
                            binding.textViewHeader.text = getText(R.string.rate_open_market)
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
        _binding = RateAppFragmentBinding.inflate(inflater, container, false)
        binding.ratingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (!fromUser) return@setOnRatingBarChangeListener
            // TODO Could requireActivity() throw an exception?
            viewModel.onRated(rating, requireActivity())
        }
        binding.buttonNeverAskAgain.setOnClickListener { viewModel.onNeverAskAgain() }
        binding.buttonNotNow.setOnClickListener { viewModel.onNotNow() }
        binding.buttonSubmit.setOnClickListener { viewModel.onFeedbackSubmit(binding.editFeedback.text.toString()) }

        // UI that will be shown if the customer is satisfied
        binding.buttonOpenMarketPlace.setOnClickListener {
//            Should this logic be located on the View Model?
//            try {
//                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
//            } catch (e: ActivityNotFoundException) {
//                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
//            }
        }
        Log.d("TAG", "returning the view ${binding.root}")
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            this.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            this.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}