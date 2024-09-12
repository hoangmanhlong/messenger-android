package com.android.kotlin.familymessagingapp.model

import androidx.annotation.StringRes
import com.android.kotlin.familymessagingapp.R

sealed class NotificationType {

    sealed class DOWNLOAD(@StringRes val name: Int) : NotificationType() {
        data object DEFAULT : DOWNLOAD(R.string.download)
        data object DOWNLOADING : DOWNLOAD(R.string.downloading)
        data object SUCCESS : DOWNLOAD(R.string.download_success)
        data object ERROR : DOWNLOAD(R.string.download_fail)
    }

}