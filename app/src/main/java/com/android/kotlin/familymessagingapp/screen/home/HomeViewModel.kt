package com.android.kotlin.familymessagingapp.screen.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.android.kotlin.familymessagingapp.firebase_services.realtime_database.AppRealtimeDatabaseService
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    firebaseServiceRepository: FirebaseServiceRepository
) : ViewModel() {

    val authenticated: LiveData<Boolean> =
        firebaseServiceRepository.authenticated.asLiveData()
}