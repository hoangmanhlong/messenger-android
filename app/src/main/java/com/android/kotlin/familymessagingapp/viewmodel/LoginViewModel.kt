package com.android.kotlin.familymessagingapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.kotlin.familymessagingapp.firebase_services.FirebaseEmailService
import com.android.kotlin.familymessagingapp.model.FirebaseCallStatus
import com.android.kotlin.familymessagingapp.utils.RegexUtils
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(firebaseEmailService: FirebaseEmailService) : ViewModel() {

    private var email: String? = ""

    private var password: String? = ""

    private val _authenticationStatus: MutableLiveData<Boolean> =
        MutableLiveData(firebaseEmailService.hasUser())
    val authenticationStatus: LiveData<Boolean> = _authenticationStatus

    private val _loginAuthenticationCallStatus: MutableLiveData<FirebaseCallStatus> =
        MutableLiveData()
    val loginAuthenticationCallStatus: LiveData<FirebaseCallStatus> = _loginAuthenticationCallStatus

    private val _loginButtonState: MutableLiveData<Boolean> = MutableLiveData(isValidInput())
    val loginButtonState: LiveData<Boolean> = _loginButtonState

    fun setEmail(email: String) {
        this.email = email
        _loginButtonState.value = isValidInput()
    }

    fun setPassword(password: String) {
        this.password = password
        _loginButtonState.value = isValidInput()
    }

    private fun isValidInput(): Boolean = !email.isNullOrBlank() && !password.isNullOrBlank()

    private fun isValidEmailAndPassword(): Boolean {
        return if (email.isNullOrBlank() || password.isNullOrBlank()) false
        else RegexUtils.isValidEmail(this.email!!) && RegexUtils.isValidPasswordLength(this.password!!)
    }

    fun login() {
        if (isValidEmailAndPassword()) {
            _loginAuthenticationCallStatus.value = FirebaseCallStatus.Calling
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email!!, password!!)
                .addOnSuccessListener {
                    _loginAuthenticationCallStatus.value = FirebaseCallStatus.Success
                    _authenticationStatus.value = FirebaseAuth.getInstance().currentUser != null
                }
                .addOnFailureListener { exception ->
                    _loginAuthenticationCallStatus.value = FirebaseCallStatus.Error(exception)
                }
        } else {
            _loginAuthenticationCallStatus.value = FirebaseCallStatus.Error(null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        email = null
        password = null
    }
}
