package com.android.kotlin.familymessagingapp.repository

import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.data.local.room.AppDatabase
import com.android.kotlin.familymessagingapp.data.local.room.SearchHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LocalDatabaseRepository(
    val appDataStore: AppDataStore,
    val appDatabase: AppDatabase,
) {

    val isTheEnglishLanguageDisplayedFlow: Flow<Boolean?> = appDataStore
        .getBooleanPreferenceFlow(AppDataStore.IS_THE_ENGLISH_LANGUAGE_DISPLAYED, true)

    fun getSearchHistories(): Flow<List<SearchHistory>> =
        appDatabase.searchHistoryDao.getSearchHistories()

    suspend fun saveSearchHistory(text: String) =
        withContext(Dispatchers.IO) {
            try {
                val searchHistory = SearchHistory(text, System.currentTimeMillis())
                appDatabase.searchHistoryDao.saveSearchHistory(searchHistory)
            } catch (e: Exception) {
                throw e
            }
        }

    suspend fun deleteSearchHistory(searchHistory: SearchHistory) =
        withContext(Dispatchers.IO) {
            try {
                appDatabase.searchHistoryDao.deleteSearchHistory(searchHistory)
            } catch (e: Exception) {
                throw e
            }
        }
}