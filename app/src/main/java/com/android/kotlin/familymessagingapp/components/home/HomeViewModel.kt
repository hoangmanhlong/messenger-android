package com.android.kotlin.familymessagingapp.components.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.android.kotlin.familymessagingapp.firebase_services.realtime.AppRealtimeDatabaseService
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.repository.FirebaseAuthenticationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    appRealtimeDatabaseService: AppRealtimeDatabaseService,
    firebaseAuthenticationRepository: FirebaseAuthenticationRepository
) : ViewModel() {

    val currentUserLiveData: LiveData<UserData?> =
        appRealtimeDatabaseService.currentUserDataFlow.asLiveData()

    val authenticated: LiveData<Boolean> =
        firebaseAuthenticationRepository.authenticated.asLiveData()
}