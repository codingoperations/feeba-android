package sample.auth

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import sample.ConfigHolder
import sample.Environment
import sample.data.RestClient
import sample.utils.PreferenceWrapper
import java.util.concurrent.atomic.AtomicBoolean

class LoginViewModel : ViewModel() {
    private val _loginStatus = SingleLiveEvent<LoginStatus>(LoginStatus.NONE)
    val loginStatus: SingleLiveEvent<LoginStatus> get() = _loginStatus

    init {
        val env = if (PreferenceWrapper.isProd) Environment.PRODUCTION else Environment.DEVELOPMENT
        val jwtToken = PreferenceWrapper.jwtToken
        if (jwtToken.isNotEmpty()) {
            // We found user jwt token, let's try to login
            RestClient.initLoggedUser(PreferenceWrapper.jwtToken, env)
            updateTokenAndProceedWithLogin(PreferenceWrapper.jwtToken)
        } else {
            // No token found, let's start fresh
            RestClient.switchEnvironment(env)
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

class SingleLiveEvent<T>(init: T) : MutableLiveData<T>(init) {

    private val pending = AtomicBoolean(false)

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, Observer<T> { t ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        })
    }

    override fun setValue(t: T?) {
        pending.set(true)
        super.setValue(t)
    }

    fun call() {
        value = null
    }
}