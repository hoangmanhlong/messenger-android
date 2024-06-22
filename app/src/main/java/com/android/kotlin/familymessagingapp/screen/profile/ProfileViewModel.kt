package com.android.kotlin.familymessagingapp.screen.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.repository.DataMemoryRepository
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseServiceRepository: FirebaseServiceRepository,
    dataMemoryRepository: DataMemoryRepository
) : ViewModel() {

    val currentUserLiveData = firebaseServiceRepository
        .appRealtimeDatabaseService
        .currentUserDataFlow
        .asLiveData()

    val isTheEnglishLanguageDisplayed = dataMemoryRepository
        .appDataStore
        .getBooleanPreferenceFlow(AppDataStore.IS_THE_ENGLISH_LANGUAGE_DISPLAYED, true)
        .asLiveData()

    val areNotificationsEnabledLiveData = dataMemoryRepository
        .appDataStore
        .getBooleanPreferenceFlow(AppDataStore.ARE_NOTIFICATION_ENABLED, true)
        .asLiveData()

    val authenticationStatus: LiveData<Boolean> =
        firebaseServiceRepository.authenticated.asLiveData()

    fun logout() = firebaseServiceRepository.signOut()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun deleteAccount() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = firebaseServiceRepository.deleteAccount()
            _isLoading.value = false
            when(result) {
                is Result.Error -> {
                    if(result.exception is FirebaseAuthRecentLoginRequiredException) {
                        logout()
                    }
                }
                is Result.Success -> {}
            }
        }
    }
}