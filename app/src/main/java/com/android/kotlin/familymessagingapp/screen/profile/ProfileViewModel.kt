package com.android.kotlin.familymessagingapp.screen.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.repository.LocalDatabaseRepository
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseServiceRepository: FirebaseServiceRepository,
    localDatabaseRepository: LocalDatabaseRepository
) : ViewModel() {

    val currentUserLiveData = firebaseServiceRepository
        .appRealtimeDatabaseService
        .currentUserDataFlow
        .asLiveData()

    val isTheEnglishLanguageDisplayed = localDatabaseRepository
        .appDataStore
        .getBooleanPreferenceFlow(AppDataStore.IS_THE_ENGLISH_LANGUAGE_DISPLAYED, true)
        .asLiveData()

    val areNotificationsEnabledLiveData = localDatabaseRepository
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
            firebaseServiceRepository.deleteAccount()
            _isLoading.value = false
        }
    }
}