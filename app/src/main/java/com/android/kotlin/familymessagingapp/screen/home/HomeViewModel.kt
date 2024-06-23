package com.android.kotlin.familymessagingapp.screen.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.firebase_services.realtime_database.AppRealtimeDatabaseService
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firebaseServiceRepository: FirebaseServiceRepository
) : ViewModel() {

    companion object {
        const val TAG = "HomeViewModel"
    }


    var isFirstLoadImage = false

    private val _searchedUser: MutableLiveData<List<UserData>> = MutableLiveData(emptyList())
    val searchedUser: LiveData<List<UserData>> = _searchedUser

    val currentUserLiveData: LiveData<UserData?> =
        firebaseServiceRepository.appRealtimeDatabaseService.currentUserDataFlow.asLiveData()

    val authenticated: LiveData<Boolean> =
        firebaseServiceRepository.authenticated.asLiveData()

    val chatRoomsLiveData: LiveData<List<ChatRoom>> =
        firebaseServiceRepository.appRealtimeDatabaseService.chatroomsFlow.asLiveData()

    fun searchByString(str: String) {
        viewModelScope.launch {
            _searchedUser.value = firebaseServiceRepository.appRealtimeDatabaseService.search(str)
        }
    }
}