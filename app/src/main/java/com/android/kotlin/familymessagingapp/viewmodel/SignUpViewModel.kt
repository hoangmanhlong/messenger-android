package com.android.kotlin.familymessagingapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.firebase_services.email_services.FirebaseEmailService
import com.android.kotlin.familymessagingapp.utils.RegexUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val firebaseEmailService: FirebaseEmailService
) : ViewModel() {
    private var email: String? = ""

    private var password: String? = ""

    private val _isValidEmail: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _isValidPassword: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _isValidEmailAndPassword: MutableLiveData<Boolean> = MutableLiveData(false)
    val isValidEmailAndPassword: LiveData<Boolean> = _isValidEmailAndPassword

    fun setEmail(email: String) {
        this.email = email
        _isValidEmail.value = RegexUtils.isValidEmail(email)
        _isValidEmailAndPassword.value = isValidEmailAndPassword()
    }

    fun setPassword(password: String) {
        this.password = password
        _isValidPassword.value = password.length >= 6
        _isValidEmailAndPassword.value = isValidEmailAndPassword()
    }

    private fun isValidEmailAndPassword(): Boolean =
        _isValidEmail.value == true && _isValidPassword.value == true

    fun signup() {
        viewModelScope.launch {
            val signupResult = firebaseEmailService.signUp(email!!, password!!)

        }
    }
}