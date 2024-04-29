package sample.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.feeba.Feeba
import io.least.core.ServerConfig
import kotlinx.coroutines.launch
import sample.ConfigHolder
import sample.DemoApplication
import sample.Environment
import sample.data.LoginResponse
import sample.data.RestClient
import sample.utils.PreferenceWrapper

class LoginViewModel : ViewModel() {
    private val _loginStatus = MutableLiveData<LoginStatus>(LoginStatus.NONE)
    val loginStatus: LiveData<LoginStatus> get() = _loginStatus

    init {
        setEnvironment(if (PreferenceWrapper.isProd) Environment.PRODUCTION else Environment.DEVELOPMENT)
        val jwtToken = PreferenceWrapper.jwtToken
        if (jwtToken.isNotEmpty()) {
            updateTokenAndProceedWithLogin(PreferenceWrapper.jwtToken)
        }
    }

    fun setEnvironment(env: Environment) {
        PreferenceWrapper.isProd = env == Environment.PRODUCTION
        ConfigHolder.setEnv(env)
        RestClient.updateEnvironment()
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
        ConfigHolder.jwtToken = jwt
        // This is how Feeba is initialized
        Feeba.init(
            DemoApplication.instance, ServerConfig(
                hostUrl = ConfigHolder.hostUrl,
                langCode = "en", // Whatever your default language is
                apiToken = ConfigHolder.jwtToken
            )
        )
        _loginStatus.value = LoginStatus.SUCCESS
    }
}

enum class LoginStatus {
    NONE, ON_AIR, SUCCESS, FAILURE
}