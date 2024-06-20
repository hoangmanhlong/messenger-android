package com.android.kotlin.familymessagingapp.screen.confirm_delete_account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.repository.FirebaseAuthenticationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfirmDeleteAccountViewModel @Inject constructor(
    private val firebaseAuthenticationRepository: FirebaseAuthenticationRepository
) : ViewModel() {

    val deleteSuccess = MutableLiveData(false)

    val isLoading = MutableLiveData(false)

    fun deleteAccount() {
        viewModelScope.launch {
            isLoading.value = true
            firebaseAuthenticationRepository.deleteAccount()
            isLoading.value = false
            deleteSuccess.value = true
        }
    }
}