package com.android.kotlin.familymessagingapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.repository.DataMemoryRepository
import com.android.kotlin.familymessagingapp.repository.FirebaseAuthenticationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
//    firebaseAuthenticationRepository: FirebaseAuthenticationRepository,
    private val dataMemoryRepository: DataMemoryRepository
) : ViewModel() {

//    val authenticated: LiveData<Boolean> =
//        firebaseAuthenticationRepository.authenticated.asLiveData()

    // Save Notification Status to local
    fun saveNotificationStatus(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataMemoryRepository.appDataStore.saveBoolean(
                AppDataStore.ARE_NOTIFICATION_ENABLED,
                enabled
            )
        }
    }

    //Save data when the user runs the app for the first time
    fun executeTheJobOnFirstRun() {
        viewModelScope.launch(Dispatchers.IO) {
            if (dataMemoryRepository.appDataStore
                    .getBooleanPreferenceFlow(AppDataStore.IS_THE_FIRST_LAUNCH, null)
                    // The terminal operator that returns the first element emitted by the flow
                    // and then cancels flow's collection
                    .first() == null
            ) {
                dataMemoryRepository.appDataStore.apply {
                    saveBoolean(AppDataStore.IS_THE_FIRST_LAUNCH, true)
                    saveBoolean(AppDataStore.IS_THE_ENGLISH_LANGUAGE_DISPLAYED, true)
                }
            }
        }
    }
}