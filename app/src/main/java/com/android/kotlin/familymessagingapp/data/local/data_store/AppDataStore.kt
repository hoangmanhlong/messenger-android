package com.android.kotlin.familymessagingapp.data.local.data_store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.android.kotlin.familymessagingapp.utils.Constant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException


private const val AUTHENTICATION_PREFERENCES_NAME = "layout_preferences"

// Create a DataStore instance using the preferencesDataStore delegate, with the Context as
// receiver.
private val Context.dataStore : DataStore<Preferences> by preferencesDataStore(
    name = AUTHENTICATION_PREFERENCES_NAME
)

class AppDataStore(private val preference_datastore: DataStore<Preferences>) {

    private val IS_AUTHENTICATE_BY_EMAIL = booleanPreferencesKey(Constant.IS_AUTHENTICATE_BY_EMAIL_KEY)

//    val preferenceFlow: Flow<Boolean> = preference_datastore.data
//        .catch {
//            if (it is IOException) {
//                it.printStackTrace()
//                emit(emptyPreferences())
//            } else {
//                throw it
//            }
//        }
//        .map { preferences ->
//            // On the first run of the app, we will use LinearLayoutManager by default
//            preferences[IS_LINEAR_LAYOUT_MANAGER] ?: true
//        }
//
    fun getBooleanPreferenceFlow(key: Preferences.Key<Boolean>): Flow<Boolean> {
        return preference_datastore.data
            .catch {
                if (it is IOException) {
                    it.printStackTrace()
                    emit(emptyPreferences())
                } else {
                    throw it
                }
            }
            .map { preferences ->
                // On the first run of the app, we will use LinearLayoutManager by default
                preferences[key] ?: true
            }
    }

    suspend fun saveBoolean(context: Context, key:  Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }
}