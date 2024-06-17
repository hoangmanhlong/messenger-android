package com.android.kotlin.familymessagingapp.screen.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.model.AuthenticationStatus
import com.android.kotlin.familymessagingapp.model.RegisterException
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.repository.FirebaseAuthenticationRepository
import com.android.kotlin.familymessagingapp.utils.StringUtils
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val firebaseAuthenticationRepository: FirebaseAuthenticationRepository
) : ViewModel() {
    private var _email: String = ""

    private var _password: String = ""

    private val _authenticationStatus: MutableLiveData<AuthenticationStatus> = MutableLiveData(AuthenticationStatus.NONE)
    val authenticationStatus: LiveData<AuthenticationStatus> = _authenticationStatus

    private val _isValidEmail: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _isValidPassword: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _buttonRegisterStatus: MutableLiveData<Boolean> =
        MutableLiveData(isValidEmailAndPassword())
    val buttonRegisterStatus: LiveData<Boolean> = _buttonRegisterStatus

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isUserExist: MutableLiveData<Boolean> = MutableLiveData()
    val isUserExist: LiveData<Boolean> = _isUserExist

    fun setEmail(email: String) {
        this._email = email
        _isValidEmail.value = StringUtils.isValidEmail(email)
        _buttonRegisterStatus.value = isValidEmailAndPassword()
    }

    fun setPassword(password: String) {
        this._password = password
        _isValidPassword.value = password.length >= 6
        _buttonRegisterStatus.value = isValidEmailAndPassword()
    }

    private fun isValidEmailAndPassword(): Boolean {
        return StringUtils.isValidEmail(this._email)
                && StringUtils.isValidPasswordLength(this._password)
                && isValidInput()
    }

    private fun isValidInput(): Boolean = _email.isNotBlank() && _password.isNotBlank()

    fun signup() {
        if (isValidEmailAndPassword()) {
            _isLoading.value = true
            viewModelScope.launch {
                val result: Result<Boolean> = firebaseAuthenticationRepository.firebaseEmailService
                    .signUp(_email, _password)
                when(result) {
                    is Result.Success -> {
                        _authenticationStatus.value = AuthenticationStatus.SUCCESS
                    }
                    is Result.Error -> {
                        _authenticationStatus.value = AuthenticationStatus.FAILURE
                        _isUserExist.value = result.exception is FirebaseAuthUserCollisionException
                    }
                }
                _isLoading.value = false
            }
        } else {
            _authenticationStatus.value = AuthenticationStatus.FAILURE
        }
    }
}