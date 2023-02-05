package io.least.case_management.ui.cases

import android.graphics.Color
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
import androidx.recyclerview.widget.DividerItemDecoration
import io.least.case_management.ServiceLocator
import io.least.case_management.databinding.CaseListBinding
import io.least.case_management.ui.CaseListFragmentFactory
import io.least.case_management.viewmodel.CaseListConfig
import io.least.core.createWithFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


private const val KEY_CONFIG = "CaseListFragmentTag"

class CaseListFragment(private val caseConfig: CaseListConfig) : Fragment() {
    companion object {

        fun show(
            supportFragmentManager: FragmentManager,
            @IdRes containerId: Int,
            classLoader: ClassLoader,
            config: CaseListConfig
        ) {
            supportFragmentManager.fragmentFactory = CaseListFragmentFactory(config)
            val fragment = supportFragmentManager.fragmentFactory.instantiate(
                classLoader,
                CaseListFragment::class.java.name
            ).apply {
                this.arguments = Bundle().apply { putParcelable(KEY_CONFIG, config) }
            }
            supportFragmentManager.beginTransaction()
                .add(containerId, fragment)
                .commit()
        }
    }

    private var _binding: CaseListBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: CaseListViewModel by viewModels {
        createWithFactory {
            CaseListViewModel(
                ServiceLocator.getXMPPTCPConnection(),
                ServiceLocator.getCaseListRepository(caseConfig.serverUrl),
                caseConfig
            )
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CaseListBinding.inflate(inflater, container, false)

        viewModel.init(requireContext())
        val caseListAdapter = CaseListAdapter {
            viewModel.createNewCase()
        }
        binding.recyclerView.adapter = caseListAdapter
        binding.recyclerView.adapter = caseListAdapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )

        // Start a coroutine in the lifecycle scope
        lifecycleScope.launch {
            // repeatOnLifecycle launches the block in a new coroutine every time the
            // lifecycle is in the STARTED state (or above) and cancels it when it's STOPPED.
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Trigger the flow and start listening for values.
                // Note that this happens when lifecycle is STARTED and stops
                // collecting when the lifecycle is STOPPED
                viewModel.uiState.collect { uiState ->
                    Log.d("TEST", "uiState received ==> $uiState")
                    // New value received
                    when (uiState) {
                        CaseListUiState.Empty -> {
                            binding.recyclerView.visibility = View.GONE
                            binding.progressLoader.visibility = View.GONE
                        }
                        CaseListUiState.Initial -> {
                            binding.textViewStatus.text = "CONNECTING"
                            binding.textViewStatus.setTextColor(Color.DKGRAY)

                            binding.recyclerView.visibility = View.GONE
                            binding.progressLoader.visibility = View.VISIBLE
                        }
                        is CaseListUiState.UpdatedCaseList -> {

                            caseListAdapter.submitList(uiState.cases)
                            binding.recyclerView.visibility = View.VISIBLE
                            binding.progressLoader.visibility = View.GONE
                        }
                        is CaseListUiState.Error -> {
                            binding.textViewStatus.text = "ERROR"
                            binding.textViewStatus.setTextColor(Color.RED)

                            binding.recyclerView.visibility = View.GONE
                            binding.progressLoader.visibility = View.GONE
                        }
                        CaseListUiState.Connected -> {
                            binding.textViewStatus.text = "CONNECTED"
                            binding.textViewStatus.setTextColor(Color.GREEN)

                            binding.recyclerView.visibility = View.VISIBLE
                            binding.progressLoader.visibility = View.GONE
                        }
                    }
                }
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchCases()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}