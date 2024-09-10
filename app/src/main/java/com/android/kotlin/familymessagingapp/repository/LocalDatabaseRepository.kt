package com.android.kotlin.familymessagingapp.repository

import android.app.Application
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.data.local.room.AppDatabase
import com.android.kotlin.familymessagingapp.data.local.room.SearchHistoryEntity
import com.android.kotlin.familymessagingapp.utils.MediaUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocalDatabaseRepository(
    application: Application,
    val appDataStore: AppDataStore,
    val appDatabase: AppDatabase,
) {

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    init {
        coroutineScope.launch {
            MediaUtils.clearCache(application)
        }
    }

    val isTheEnglishLanguageDisplayedFlow: Flow<Boolean?> = appDataStore
        .getBooleanPreferenceFlow(AppDataStore.IS_THE_ENGLISH_LANGUAGE_DISPLAYED, true)

    fun getSearchHistories(): Flow<List<SearchHistoryEntity>> =
        appDatabase.searchHistoryDao.getSearchHistories()

    suspend fun saveSearchHistory(text: String) =
        withContext(Dispatchers.IO) {
            try {
                val searchHistoryEntity = SearchHistoryEntity(text, System.currentTimeMillis())
                appDatabase.searchHistoryDao.saveSearchHistory(searchHistoryEntity)
            } catch (e: Exception) {
                throw e
            }
        }

    suspend fun deleteSearchHistory(searchHistoryEntity: SearchHistoryEntity) =
        withContext(Dispatchers.IO) {
            try {
                appDatabase.searchHistoryDao.deleteSearchHistory(searchHistoryEntity)
            } catch (e: Exception) {
                throw e
            }
        }

    suspend fun clearAllSearchHistories() =
        withContext(Dispatchers.IO) {
            try {
                appDatabase.searchHistoryDao.deleteAllSearchHistories()
            } catch (e: Exception) {
                throw e
            }
        }
}
