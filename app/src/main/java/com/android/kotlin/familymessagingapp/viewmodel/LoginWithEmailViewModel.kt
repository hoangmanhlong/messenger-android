package com.android.kotlin.familymessagingapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.repository.FirebaseAuthenticationRepository
import com.android.kotlin.familymessagingapp.utils.RegexUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginWithEmailViewModel @Inject constructor(
    private val _firebaseAuthenticationRepository: FirebaseAuthenticationRepository
) : ViewModel() {

    /**
     * Store current email entered by the user
     */
    private var _email: String? = ""

    /**
     * Store current password entered by the user
     */
    private var _password: String? = ""

    private val _loadingStatus: MutableLiveData<Boolean> = MutableLiveData(false)
    val loadingStatus: LiveData<Boolean> = _loadingStatus

    private val _loginButtonState: MutableLiveData<Boolean> = MutableLiveData(isValidInput())
    val loginButtonState: LiveData<Boolean> = _loginButtonState

    private val _authenticationStatus: MutableLiveData<Boolean> =
        MutableLiveData(_firebaseAuthenticationRepository.hasUser())
    val authenticationStatus: LiveData<Boolean> = _authenticationStatus


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
        return if (_email.isNullOrBlank() || _password.isNullOrBlank()) false
        else RegexUtils.isValidEmail(this._email!!) && RegexUtils.isValidPasswordLength(this._password!!)
    }


    /**
     * Handle login button click
     * Authenticate email and password with firebase authentication
     */
    fun loginWithEmail() {
        if (isValidEmailAndPassword()) {
            _loadingStatus.value = true
            viewModelScope.launch {
                _authenticationStatus.value = _firebaseAuthenticationRepository
                    .firebaseEmailService.signIn(_email!!, _password!!)
                _loadingStatus.value = false
            }
        } else {
            _authenticationStatus.value = false
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