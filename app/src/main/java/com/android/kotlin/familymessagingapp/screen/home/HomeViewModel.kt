package com.android.kotlin.familymessagingapp.screen.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.data.local.room.SearchHistory
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import com.android.kotlin.familymessagingapp.repository.LocalDatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firebaseServiceRepository: FirebaseServiceRepository,
    private val localDatabaseRepository: LocalDatabaseRepository
) : ViewModel() {

    companion object {
        val TAG: String = HomeViewModel::class.java.simpleName
    }

    val authenticated: LiveData<Boolean> = firebaseServiceRepository.authenticated.asLiveData()

    val currentUserLiveData: LiveData<UserData?> = firebaseServiceRepository
        .firebaseRealtimeDatabaseService
        .currentUserDataFlow
        .asLiveData()

    val chatRoomsLiveData: LiveData<List<ChatRoom>> = firebaseServiceRepository
        .firebaseRealtimeDatabaseService.chatRoomsFlow
        .distinctUntilChanged() // Only update when data changes
        .asLiveData()

    val searchHistories: LiveData<List<SearchHistory>> = localDatabaseRepository
        .getSearchHistories()
        .asLiveData()

    private var currentKeyword = ""

    var isFragmentCreatedFirstTime = false

    private val _searchResultList: MutableLiveData<List<UserData>> = MutableLiveData(emptyList())
    val searchResultList: LiveData<List<UserData>> = _searchResultList

    var isShowSearchResult = false
        private set

    init {
        currentUserLiveData.observeForever { userdata ->
            if (userdata != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    localDatabaseRepository.appDataStore.saveBoolean(
                        AppDataStore.ENABLED_AI,
                        userdata.settings?.enabledAI ?: false
                    )
                }
            } else {
                firebaseServiceRepository.signOut()
            }
        }
    }

    fun setIsShowSearchResult(isShow: Boolean) {
        isShowSearchResult = isShow
    }

    fun searchKeyword(keyword: String) {
        isShowSearchResult = true
        if (keyword.isEmpty()) {
            _searchResultList.value = emptyList()
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                localDatabaseRepository.saveSearchHistory(keyword)
            }
            // if new keyword and old keyword is same then skip
            if (currentKeyword != keyword) {
                currentKeyword = keyword
                //            _searchedUser.value = emptyList() // clear old list when start query
                viewModelScope.launch {
                    _searchResultList.value =
                        firebaseServiceRepository.firebaseRealtimeDatabaseService.search(keyword)
                }
            } else {
                _searchResultList.value = searchResultList.value
            }
        }
    }

    fun deleteSearchHistory(searchHistory: SearchHistory) {
        viewModelScope.launch(Dispatchers.IO) {
            localDatabaseRepository.deleteSearchHistory(searchHistory)
        }
    }

    fun clearAllSearchHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            localDatabaseRepository.clearAllSearchHistories()
        }
    }
}