package com.android.kotlin.familymessagingapp.repository

import android.app.Application
import android.net.Uri
import androidx.lifecycle.liveData
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.data.local.room.AppDatabase
import com.android.kotlin.familymessagingapp.data.local.room.SearchHistoryEntity
import com.android.kotlin.familymessagingapp.model.FileData
import com.android.kotlin.familymessagingapp.model.MediaData
import com.android.kotlin.familymessagingapp.model.NotificationType
import com.android.kotlin.familymessagingapp.model.Result
import com.android.kotlin.familymessagingapp.utils.MediaUtils
import com.android.kotlin.familymessagingapp.utils.makeStatusNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okio.IOException

class LocalDatabaseRepository(
    val application: Application,
    val appDataStore: AppDataStore,
    val appDatabase: AppDatabase,
) {

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job + Dispatchers.Main)

    init {
        clearCache()
    }

    private fun clearCache() {
        coroutineScope.launch {
            MediaUtils.clearCache(application)
        }
    }

    fun updateFileDataFromUri(list: List<Uri>): List<FileData> {
        // Chạy các tác vụ đồng thời
        return runBlocking {
            // Sử dụng async để thực hiện các tác vụ đồng thời
            val deferredFileData = list.map { uri ->
                async {
                    val fileType = MediaUtils.getFileType(application, uri)
                    val fileName = MediaUtils.getFileName(application, uri)
                    val fileSize = MediaUtils.getFileSize(application, uri)
                    val mime = MediaUtils.getMimeType(application, uri)
                    FileData(uri, fileType, fileName, fileSize, mime)
                }
            }

            // Chờ cho tất cả các tác vụ hoàn thành và trả kết quả
            deferredFileData.awaitAll()
        }
    }

    fun downloadFiles(list: List<MediaData>) {
        coroutineScope.launch {
            makeStatusNotification(
                context = application,
                data = null,
                notificationType = NotificationType.DOWNLOAD.DOWNLOADING
            )
            val deferredDownload = list.map { media ->
                async {
                    if (media.url.isNullOrEmpty()) return@async Result.Error(IOException())
                    MediaUtils.downloadFile(media.url, media.fileName)
                }
            }.awaitAll()

            val lastSuccessfulUri = deferredDownload
                .filterIsInstance<Result.Success<Uri>>() // Lọc chỉ những kết quả thành công
                .lastOrNull()?.data // Lấy URI từ kết quả thành công

            val success = deferredDownload.all { it is Result.Success }
            if (success && lastSuccessfulUri != null) {
                makeStatusNotification(
                    context = application,
                    data = lastSuccessfulUri,
                    notificationType = NotificationType.DOWNLOAD.SUCCESS
                )
            } else {
                makeStatusNotification(
                    context = application,
                    data = null,
                    notificationType = NotificationType.DOWNLOAD.ERROR
                )
            }
        }
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
