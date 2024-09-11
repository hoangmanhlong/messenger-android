package com.android.kotlin.familymessagingapp.repository

import android.app.Application
import android.net.Uri
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.data.local.room.AppDatabase
import com.android.kotlin.familymessagingapp.data.local.room.SearchHistoryEntity
import com.android.kotlin.familymessagingapp.model.FileData
import com.android.kotlin.familymessagingapp.utils.MediaUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocalDatabaseRepository(
    val application: Application,
    val appDataStore: AppDataStore,
    val appDatabase: AppDatabase,
) {

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    init {
        clearCache()
    }

    private fun clearCache() {
        coroutineScope.launch {
            MediaUtils.clearCache(application)
        }
    }

    fun updateFileDataFromUri(list: List<Uri>): List<FileData> {
        val newItems = mutableListOf<FileData>()

        list.map {
            val fileType = MediaUtils.getFileType(application, it)
            val fileName = MediaUtils.getFileName(application, it)
            newItems.add(FileData(it, fileType, fileName))
        }

        return newItems
    }

    fun deleteTakenPhotoFromCamera(uris: List<Uri>) {
        if (uris.isEmpty()) return
        uris.map { uri -> MediaUtils.deleteUriInCache(application, uri) }
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
