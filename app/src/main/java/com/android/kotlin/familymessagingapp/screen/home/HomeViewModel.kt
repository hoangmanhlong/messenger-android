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

sealed class SearchViewStatus {
    object ShowSearchHistory : SearchViewStatus()
    object ShowSearchResult : SearchViewStatus()
    object Other : SearchViewStatus()
}

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

    private val _searchViewStatus: MutableLiveData<SearchViewStatus> = MutableLiveData(SearchViewStatus.ShowSearchHistory)
    val searchViewStatus: LiveData<SearchViewStatus> = _searchViewStatus

    private var currentKeyword = ""

    var isFragmentCreatedFirstTime = false

    private val _searchResultList: MutableLiveData<List<UserData>> = MutableLiveData(emptyList())
    val searchResultList: LiveData<List<UserData>> = _searchResultList

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

    fun setSearchViewStatus(status: SearchViewStatus) {
        _searchViewStatus.value = status
    }

    fun searchKeyword(keyword: String) {
        viewModelScope.launch {
            if (keyword.isEmpty()) {
                _searchResultList.value = emptyList()
            } else {

                localDatabaseRepository.saveSearchHistory(keyword)

                // if new keyword and old keyword is same then skip
                if (currentKeyword != keyword) {
                    currentKeyword = keyword
                    //            _searchedUser.value = emptyList() // clear old list when start query

                    _searchResultList.value =
                        firebaseServiceRepository.firebaseRealtimeDatabaseService.search(keyword)

                } else {
                    _searchResultList.value = searchResultList.value
                }
            }
            _searchViewStatus.value = SearchViewStatus.ShowSearchResult
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