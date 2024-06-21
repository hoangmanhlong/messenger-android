package com.android.kotlin.familymessagingapp.screen.login_email

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.model.AuthenticationStatus
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import com.android.kotlin.familymessagingapp.utils.StringUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginEmailViewModel @Inject constructor(
    private val firebaseServiceRepository: FirebaseServiceRepository
) : ViewModel() {

    /**
     * Store current email entered by the user
     */
    private var _email: String? = ""

    /**
     * Store current password entered by the user
     */
    private var _password: String? = ""

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loginButtonState: MutableLiveData<Boolean> = MutableLiveData(isValidInput())
    val loginButtonState: LiveData<Boolean> = _loginButtonState

    private val _authenticationStatus: MutableLiveData<AuthenticationStatus> =
        MutableLiveData(AuthenticationStatus.NONE)
    val authenticationStatus: LiveData<AuthenticationStatus> = _authenticationStatus

    fun setAuthenticationStatusNone() {
        _authenticationStatus.value = AuthenticationStatus.NONE
    }

    /**
     * Called when the user enters a new email from email text field.
     */
    fun setEmail(email: String) {
        this._email = email
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
    private fun isValidInput(): Boolean = !_email.isNullOrBlank() && !_password.isNullOrBlank()

    /**
     * Check if email and password are valid
     */
    private fun isValidEmailAndPassword(): Boolean {
        return StringUtils.isValidEmail(this._email!!)
                && StringUtils.isValidPasswordLength(this._password!!)
                && isValidInput()
    }

    /**
     * Handle login button click
     * Authenticate email and password with firebase authentication
     */
    fun loginWithEmail() {
        if (isValidEmailAndPassword()) {
            _isLoading.value = true
            viewModelScope.launch {
                val result  = firebaseServiceRepository
                    .firebaseEmailService
                    .signIn(_email!!, _password!!)
                if (result) _authenticationStatus.value = AuthenticationStatus.SUCCESS
                else _authenticationStatus.value = AuthenticationStatus.FAILURE
                _isLoading.value = false
            }
        } else {
            _authenticationStatus.value = AuthenticationStatus.FAILURE
        }
    }

    /**
     * Called when the ViewModel is destroyed.
     */
    override fun onCleared() {
        super.onCleared()
        _email = null
        _password = null
    }
}