package com.android.kotlin.familymessagingapp.screen.select_language

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.android.kotlin.familymessagingapp.data.local.data_store.AppDataStore
import com.android.kotlin.familymessagingapp.repository.DataMemoryRepository
import com.android.kotlin.familymessagingapp.utils.Constant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectLanguageViewModel @Inject constructor(
    private val dataMemoryRepository: DataMemoryRepository
) : ViewModel() {

    val isTheEnglishLanguageDisplayed: LiveData<Boolean?> = dataMemoryRepository
        .appDataStore
        .getBooleanPreferenceFlow(AppDataStore.IS_THE_ENGLISH_LANGUAGE_DISPLAYED, true)
        .asLiveData()

    private val _cancelFragment = MutableLiveData(false)
    val cancelFragment: LiveData<Boolean> = _cancelFragment

    private var _isTheEnglishLanguageSelected = isTheEnglishLanguageDisplayed.value ?: true

    // Update selected by User
    fun isTheEnglishLanguageSelected(isTheEnglishLanguageSelected: Boolean) {
        _isTheEnglishLanguageSelected = isTheEnglishLanguageSelected
    }

    fun changeLanguage() {
        // if selected language different current language saved in Local then make the change language
        // else close fragment
        if (_isTheEnglishLanguageSelected != isTheEnglishLanguageDisplayed.value) {
            viewModelScope.launch {
                // Save to Local
                dataMemoryRepository.appDataStore.saveBoolean(
                    AppDataStore.IS_THE_ENGLISH_LANGUAGE_DISPLAYED,
                    _isTheEnglishLanguageSelected
                )
                // Change Display Language
                val appLocale = LocaleListCompat.forLanguageTags(
                    if (_isTheEnglishLanguageSelected) Constant.ENGLISH_COUNTRY_CODE
                    else Constant.VIETNAM_COUNTRY_CODE
                )
                AppCompatDelegate.setApplicationLocales(appLocale);
            }
        } else {
            // close Fragment
            _cancelFragment.value = true
        }
    }
}