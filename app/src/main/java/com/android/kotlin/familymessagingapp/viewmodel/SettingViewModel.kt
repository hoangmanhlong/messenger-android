package com.android.kotlin.familymessagingapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.firebase_services.FirebaseEmailService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val firebaseEmailService: FirebaseEmailService
) : ViewModel() {
    fun logout() {
        viewModelScope.launch { firebaseEmailService.signOut() }
    }

    fun deleteAccount() {
        viewModelScope.launch { firebaseEmailService.deleteAccount() }
    }
}