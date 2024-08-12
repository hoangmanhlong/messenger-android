package com.android.kotlin.familymessagingapp.screen.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.data.local.room.SearchHistoryEntity
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import com.android.kotlin.familymessagingapp.repository.LocalDatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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

    val authenticateState: LiveData<Boolean?> = firebaseServiceRepository.authenticateState

    val currentUserLiveData: LiveData<UserData?> = firebaseServiceRepository
        .firebaseRealtimeDatabaseService
        .publicUserData

    val chatRoomsLiveData: LiveData<List<ChatRoom>?> = firebaseServiceRepository
        .firebaseRealtimeDatabaseService
        .chatRooms
        .asLiveData()

    val searchHistories: LiveData<List<SearchHistoryEntity>> = localDatabaseRepository
        .getSearchHistories()
        .asLiveData()

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _searchViewStatus: MutableLiveData<SearchViewStatus> =
        MutableLiveData(SearchViewStatus.ShowSearchHistory)
    val searchViewStatus: LiveData<SearchViewStatus> = _searchViewStatus

    private var currentKeyword = ""

    var isFragmentCreatedFirstTime = false

    private val _searchResultList: MutableLiveData<List<UserData>> = MutableLiveData(emptyList())
    val searchResultList: LiveData<List<UserData>> = _searchResultList

    init {
        authenticateState.observeForever {
            if (it == true) {
                currentUserLiveData.observeForever { userdata ->
                    if (userdata == null) firebaseServiceRepository.signOut()
                }
            }
        }
    }

    fun setIsLoadingStatus(value: Boolean) {
        _isLoading.value = value
    }

    fun setSearchViewStatus(status: SearchViewStatus) {
        _searchViewStatus.value = status
    }

    fun searchKeyword(keyword: String, searchByUid: Boolean) {
        viewModelScope.launch {
            if (keyword.isEmpty()) {
                _searchResultList.value = emptyList()
            } else {

                if (!searchByUid) localDatabaseRepository.saveSearchHistory(keyword)

                // if new keyword and old keyword is same then skip
                if (currentKeyword != keyword) {
                    currentKeyword = keyword
                    //            _searchedUser.value = emptyList() // clear old list when start query

                    _searchResultList.value =
                        firebaseServiceRepository.firebaseRealtimeDatabaseService.search(keyword, searchByUid)

                } else {
                    _searchResultList.value = searchResultList.value
                }
            }
            _searchViewStatus.value = SearchViewStatus.ShowSearchResult
        }
    }

    fun deleteSearchHistory(searchHistoryEntity: SearchHistoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            localDatabaseRepository.deleteSearchHistory(searchHistoryEntity)
        }
    }

    fun clearAllSearchHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            localDatabaseRepository.clearAllSearchHistories()
        }
    }
}