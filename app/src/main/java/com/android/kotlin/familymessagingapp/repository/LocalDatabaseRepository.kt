package com.android.kotlin.familymessagingapp.repository

import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import kotlinx.coroutines.flow.Flow

class LocalDatabaseRepository(val appDataStore: AppDataStore) {

    val isTheEnglishLanguageDisplayedFlow: Flow<Boolean?> = appDataStore
        .getBooleanPreferenceFlow(AppDataStore.IS_THE_ENGLISH_LANGUAGE_DISPLAYED, true)
}
