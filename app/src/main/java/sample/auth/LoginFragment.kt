package sample.auth

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import io.least.demo.R
import io.least.demo.databinding.FragmentLoginBinding
import sample.ConfigHolder
import sample.Environment
import sample.utils.PreferenceWrapper

class LoginFragment : Fragment() {
    companion object {
        fun newInstance() = LoginFragment()
    }

    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.switchEnv.isChecked = PreferenceWrapper.isProd
        binding.buttonLogin.setOnClickListener {
            viewModel.login(
                binding.editTextEmail.text.toString(),
                binding.editTextPassword.text.toString()
            )
        }
        binding.switchEnv.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setEnvironment(if (isChecked) Environment.PRODUCTION else Environment.DEVELOPMENT)
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        viewModel.loginStatus.observe(viewLifecycleOwner) {
            if (it == LoginStatus.SUCCESS) {
                findNavController().navigate(R.id.action_open_project_list)
            } else if (it == LoginStatus.ON_AIR) {
                binding.buttonLogin.isEnabled = false
            } else {
                binding.buttonLogin.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}