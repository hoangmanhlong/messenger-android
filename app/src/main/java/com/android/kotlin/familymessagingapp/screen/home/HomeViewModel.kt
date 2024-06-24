package com.android.kotlin.familymessagingapp.screen.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import com.android.kotlin.familymessagingapp.utils.StringUtils
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

    private var currentKeyword = ""

    var isFirstLoadImage = false

    private val _searchedUser: MutableLiveData<List<UserData>> = MutableLiveData(emptyList())
    val searchedUser: LiveData<List<UserData>> = _searchedUser

    val currentUserLiveData: LiveData<UserData?> =
        firebaseServiceRepository.appRealtimeDatabaseService.currentUserDataFlow.asLiveData()

    val authenticated: LiveData<Boolean> =
        firebaseServiceRepository.authenticated.asLiveData()

    val chatRoomsLiveData: LiveData<List<ChatRoom>> =
        firebaseServiceRepository.appRealtimeDatabaseService.chatroomsFlow.asLiveData()

    fun searchKeyword(keyword: String) {
        if (keyword.isEmpty()) {
            _searchedUser.value = emptyList()
        } else {
            // if new keyword and old keyword is same then skip
            if (currentKeyword != keyword) {
                currentKeyword = keyword
                //            _searchedUser.value = emptyList() // clear old list when start query
                viewModelScope.launch {
                    _searchedUser.value = firebaseServiceRepository.appRealtimeDatabaseService.search(keyword)
                }
            }
        }
    }
}