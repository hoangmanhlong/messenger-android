package com.android.kotlin.familymessagingapp.data.local.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.repository.BackendServiceRepository
import com.android.kotlin.familymessagingapp.utils.Constant
import com.android.kotlin.familymessagingapp.utils.StringUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SaveFCMTokenToLocalAndSendToServerWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dataStore: AppDataStore,
    private val backendServiceRepository: BackendServiceRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val fcmToken = inputData.getString(Constant.FCM_TOKEN_KEY) ?: return Result.failure()
            dataStore.saveString(AppDataStore.TOKEN, fcmToken)
            val userToken = dataStore.getStringPreferenceFlow(AppDataStore.FCM_TOKEN, null).first()
            userToken?.let { token ->
                backendServiceRepository.sendFCMToken(
                    userToken = StringUtils.generateBearerToken(token),
                    fcmToken = StringUtils.generateBearerToken(fcmToken)
                )
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}