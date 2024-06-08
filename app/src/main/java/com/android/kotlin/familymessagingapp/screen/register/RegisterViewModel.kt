package com.android.kotlin.familymessagingapp.screen.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.data.remote.dto.res.ObjectResponse
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.repository.AppRepository
import com.android.kotlin.familymessagingapp.utils.StringUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {
    private var username: String? = ""

    private var password: String? = ""

    private val _isRegisterSuccess: MutableLiveData<Boolean> = MutableLiveData()
    val isRegisterSuccess: LiveData<Boolean> = _isRegisterSuccess

    private val _isValidEmail: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _isValidPassword: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _buttonRegisterStatus: MutableLiveData<Boolean> =
        MutableLiveData(isValidUsernameAndPassword())
    val buttonRegisterStatus: LiveData<Boolean> = _buttonRegisterStatus

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun setUsername(email: String) {
        this.username = email
        _isValidEmail.value = StringUtils.isValidEmail(email)
        _buttonRegisterStatus.value = isValidUsernameAndPassword()
    }

    fun setPassword(password: String) {
        this.password = password
        _isValidPassword.value = password.length >= 6
        _buttonRegisterStatus.value = isValidUsernameAndPassword()
    }

    private fun isValidUsernameAndPassword(): Boolean =
        _isValidEmail.value == true && _isValidPassword.value == true

    fun signup() {
        _isLoading.value = true
        viewModelScope.launch {
            val result: Result<ObjectResponse> = try {
                appRepository.register(username!!, password!!)
            } catch (e: Exception) {
                Result.Error(e)
            }
            when (result) {
                is Result.Success<ObjectResponse> -> {
                    _isLoading.value = false
                    _isRegisterSuccess.value = true
                }

                else -> {
                    _isLoading.value = false
                    _isRegisterSuccess.value = false
                }
            }
        }
    }
}