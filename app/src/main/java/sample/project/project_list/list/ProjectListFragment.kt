package sample.project.project_list.list

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import io.feeba.Feeba
import io.feeba.lifecycle.LogLevel
import io.feeba.lifecycle.Logger
import io.least.core.ServerConfig
import io.least.demo.R
import io.least.demo.databinding.FragmentProjectListBinding
import sample.ConfigHolder
import sample.DemoApplication

class ProjectListFragment : Fragment() {

    private var _binding: FragmentProjectListBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: ProjectListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProjectListBinding.inflate(inflater, container, false)
        return binding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[ProjectListViewModel::class.java]

        viewModel.projectList.observe(viewLifecycleOwner) { status ->
            when (status) {
                is ProjectListStatus.Initialized -> {
                }
                is ProjectListStatus.Fetching -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is ProjectListStatus.Failure -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error: ${status.errorMessage}", Toast.LENGTH_LONG).show()
                }
                is ProjectListStatus.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.projectList.layoutManager = LinearLayoutManager(requireContext())
                    binding.projectList.adapter = ProjectListAdapter(status.projects) {
                        // This is how Feeba is initialized
                        Logger.log(LogLevel.DEBUG, "ProjectListFragment::initiating Feeba...")
                        Feeba.updateServerConfig(
                            ServerConfig(
                                langCode = "en",
                                apiToken = it.tokens.first().jwtToken, // Whatever your default language is
                                hostUrl = ConfigHolder.baseUrl
                            )
                        )
                        findNavController().navigate(R.id.action_open_showcase, Bundle().apply {
                            putSerializable("project", it)
                        })
                    }
                }
            }
        }
        viewModel.fetchProjects()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}