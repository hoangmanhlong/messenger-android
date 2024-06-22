package com.android.kotlin.familymessagingapp.screen.select_language

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.android.kotlin.familymessagingapp.repository.DataMemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SelectLanguageViewModel @Inject constructor(
    dataMemoryRepository: DataMemoryRepository
) : ViewModel() {

    val isTheEnglishLanguageDisplayed: LiveData<Boolean?> = dataMemoryRepository
        .isTheEnglishLanguageDisplayedFlow
        .asLiveData()
}