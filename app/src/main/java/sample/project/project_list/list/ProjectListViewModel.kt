package sample.project.project_list.list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import sample.auth.LoginStatus
import sample.data.Project
import sample.data.RestClient

class ProjectListViewModel : ViewModel() {
    private val _projectList = MutableLiveData<ProjectListStatus>(ProjectListStatus.Initialized)
    val projectList: LiveData<ProjectListStatus> get() = _projectList

    init {
        Log.d("ProjectListFragment::", "onCreateView")
    }
    fun fetchProjects() {
        viewModelScope.launch {
            try {
                _projectList.value = ProjectListStatus.Fetching
                val result = RestClient.getProjects()
                _projectList.value = ProjectListStatus.Success(result)
            } catch (e: Throwable) {
                Log.e("ProjectListViewModel", "Error fetching projects", e)
                _projectList.value = ProjectListStatus.Failure(e.message ?: "Unknown error")
            }

        }

    }
}

sealed class ProjectListStatus {
    object Initialized : ProjectListStatus()
    object Fetching : ProjectListStatus()
    class Failure(val errorMessage: String) : ProjectListStatus()
    class Success(val projects: List<Project>) : ProjectListStatus()
}

