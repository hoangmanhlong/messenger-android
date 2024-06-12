package com.android.kotlin.familymessagingapp.screen.login_username_password

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.data.remote.dto.res.LoginRes
import com.android.kotlin.familymessagingapp.data.remote.dto.res.ObjectResponse
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.repository.AppRepository
import com.android.kotlin.familymessagingapp.repository.FirebaseAuthenticationRepository
import com.android.kotlin.familymessagingapp.utils.StringUtils
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginWithUsernamePasswordViewModel @Inject constructor(
    private val firebaseAuthenticationRepository: FirebaseAuthenticationRepository,
    private val appRepository: AppRepository,
    private val appDataStore: AppDataStore
) : ViewModel() {

    /**
     * Store current email entered by the user
     */
    private var _username: String? = ""

    /**
     * Store current password entered by the user
     */
    private var _password: String? = ""

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loginButtonState: MutableLiveData<Boolean> = MutableLiveData(isValidInput())
    val loginButtonState: LiveData<Boolean> = _loginButtonState

    private val _isLoginSuccess: MutableLiveData<Boolean> = MutableLiveData()
    val isLoginSuccess: LiveData<Boolean> = _isLoginSuccess


    /**
     * Called when the user enters a new email from email text field.
     */
    fun setUsername(email: String) {
        this._username = email
        // update button state
        _loginButtonState.value = isValidInput()
    }

    /**
     * Called when the user enters a new email from password text field.
     */
    fun setPassword(password: String) {
        this._password = password
        // update button state
        _loginButtonState.value = isValidInput()
    }

    /**
     * This method used to button login state when user enter email and password
     * Input is valid if email and password are not null or blank
     */
    private fun isValidInput(): Boolean = !_username.isNullOrBlank() && !_password.isNullOrBlank()

    /**
     * Check if email and password are valid
     */
    private fun isValidEmailAndPassword(): Boolean {
        return if (_username.isNullOrBlank() || _password.isNullOrBlank()) false
        else StringUtils.isValidEmail(this._username!!) && StringUtils.isValidPasswordLength(this._password!!)
    }

    /**
     * Handle login button click
     * Authenticate email and password with firebase authentication
     */
    fun loginWithEmail() {
        if (isValidEmailAndPassword()) {
            _isLoading.value = true
            viewModelScope.launch {
                val result = try {
                    appRepository.login(_username!!, _password!!)
                } catch (e: Exception) {
                    Result.Error(e)
                }

                when (result) {
                    is Result.Success<ObjectResponse> -> {
                        try {
                            val data = result.data as LoginRes
                            data.token?.let {
                                appDataStore.saveString(AppDataStore.TOKEN, it)
                            }

                            _isLoading.value = false
                        } catch (e: Exception) {
                            _isLoading.value = false
                            _isLoginSuccess.value = false
                        }
                    }

                    else -> {
                        _isLoading.value = false
                        _isLoginSuccess.value = false
                    }
                }
            }
        }
    }

    /**
     * Called when the ViewModel is destroyed.
     */
    override fun onCleared() {
        super.onCleared()
        _username = null
        _password = null
    }
}