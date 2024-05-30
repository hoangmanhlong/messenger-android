package com.android.kotlin.familymessagingapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.repository.DataMemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dataMemoryRepository: DataMemoryRepository
) : ViewModel() {
    fun saveNotificationStatus(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            dataMemoryRepository.appDataStore.saveBoolean(
                context,
                AppDataStore.ARE_NOTIFICATION_ENABLED,
                enabled
            )
        }
    }
}