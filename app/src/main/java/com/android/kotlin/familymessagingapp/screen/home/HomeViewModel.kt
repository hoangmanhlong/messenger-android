package com.android.kotlin.familymessagingapp.screen.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.data.local.room.SearchHistory
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.UserData
import com.android.kotlin.familymessagingapp.repository.FirebaseServiceRepository
import com.android.kotlin.familymessagingapp.repository.LocalDatabaseRepository
import com.google.android.material.search.SearchView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchViewState { Hidden, Shown, Showing }

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firebaseServiceRepository: FirebaseServiceRepository,
    private val localDatabaseRepository: LocalDatabaseRepository
) : ViewModel() {

    val authenticated: LiveData<Boolean> =
        firebaseServiceRepository.authenticated.asLiveData()

    companion object {
        const val TAG = "HomeViewModel"
    }

    private var currentKeyword = ""

    var isFirstLoadImage = false

    private val _searchedUser: MutableLiveData<List<UserData>> = MutableLiveData(emptyList())
    val searchedUser: LiveData<List<UserData>> = _searchedUser

    val searchHistories: LiveData<List<SearchHistory>> = localDatabaseRepository
        .getSearchHistories().asLiveData()

    private val _searchViewState = MutableLiveData(SearchView.TransitionState.HIDDEN)
    val searchViewState: LiveData<SearchView.TransitionState> = _searchViewState

    val _isShowingSearchResult: MutableLiveData<Boolean> = MutableLiveData(false)
    val isShowingSearchResult: LiveData<Boolean> = _isShowingSearchResult

    val currentUserLiveData: LiveData<UserData?> =
        firebaseServiceRepository.appRealtimeDatabaseService.currentUserDataFlow.asLiveData()

    val chatRoomsLiveData: LiveData<List<ChatRoom>> =
        firebaseServiceRepository.appRealtimeDatabaseService.chatRoomsFlow.asLiveData()

    fun setShowSearchedUserResult(show: Boolean) {
        _isShowingSearchResult.value = show
    }

    fun setSearchViewState(searchViewState: SearchView.TransitionState) {
        _searchViewState.value = searchViewState
    }

    fun searchKeyword(keyword: String) {
        _isShowingSearchResult.value = true
        if (keyword.isEmpty()) {
            _searchedUser.value = emptyList()
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                localDatabaseRepository.saveSearchHistory(keyword)
            }
            // if new keyword and old keyword is same then skip
            if (currentKeyword != keyword) {
                currentKeyword = keyword
                //            _searchedUser.value = emptyList() // clear old list when start query
                viewModelScope.launch {
                    _searchedUser.value =
                        firebaseServiceRepository.appRealtimeDatabaseService.search(keyword)
                }
            }
        }
    }

    fun deleteSearchHistory(searchHistory: SearchHistory) {
        viewModelScope.launch(Dispatchers.IO) {
            localDatabaseRepository.deleteSearchHistory(searchHistory)
        }
    }
}