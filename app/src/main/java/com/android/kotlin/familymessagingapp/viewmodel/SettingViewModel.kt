package com.android.kotlin.familymessagingapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.repository.FirebaseAuthenticationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val _firebaseAuthenticationRepository: FirebaseAuthenticationRepository
) : ViewModel() {


    fun logout() {

    }
    private fun logoutEmailAccount() {
        viewModelScope.launch { _firebaseAuthenticationRepository.firebaseEmailService.signOut() }
    }

    private fun logoutGoogleAccount() {
        viewModelScope.launch { _firebaseAuthenticationRepository.firebaseGoogleService.signOut() }
    }
}