package sample.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import sample.ConfigHolder
import sample.Environment
import sample.data.RestClient
import sample.utils.PreferenceWrapper

class LoginViewModel : ViewModel() {
    private val _loginStatus = MutableLiveData<LoginStatus>(LoginStatus.NONE)
    val loginStatus: LiveData<LoginStatus> get() = _loginStatus

    init {
        val env = if (PreferenceWrapper.isProd) Environment.PRODUCTION else Environment.DEVELOPMENT
        val jwtToken = PreferenceWrapper.jwtToken
        if (jwtToken.isNotEmpty()) {
            // We found user jwt token, let's try to login
            RestClient.initLoggedUser(PreferenceWrapper.jwtToken, env)
            updateTokenAndProceedWithLogin(PreferenceWrapper.jwtToken)
        }
    }

    fun changeEnvironment(env: Environment) {
        RestClient.switchEnvironment(env)
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _loginStatus.value = LoginStatus.ON_AIR
                val result = RestClient.login(email, password)
                updateTokenAndProceedWithLogin(result.jwt)
            } catch (e: Throwable) {
                Log.e("LoginViewModel", "login: $e")
                _loginStatus.value = LoginStatus.FAILURE
            }
        }
    }

    private fun updateTokenAndProceedWithLogin(jwt: String) {
        PreferenceWrapper.jwtToken = jwt
        _loginStatus.value = LoginStatus.SUCCESS
    }
}

enum class LoginStatus {
    NONE, ON_AIR, SUCCESS, FAILURE
}