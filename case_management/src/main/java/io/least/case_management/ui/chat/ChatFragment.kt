package io.least.case_management.ui.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.least.case_management.ServiceLocator
import io.least.core.createWithFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {
    private val viewModel: ChatListViewModel by viewModels {
        createWithFactory {
            ChatListViewModel(ServiceLocator.getXMPPTCPConnection())
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
                        is ChatUiState.Initial -> {
                        }
                        is ChatUiState.ChatUpdated -> TODO()
                        ChatUiState.Empty -> TODO()
                    }
                }
            }
        }
    }
}