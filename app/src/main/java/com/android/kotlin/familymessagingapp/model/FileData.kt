package com.android.kotlin.familymessagingapp.model

import android.net.Uri
import android.os.Parcelable
import com.android.kotlin.familymessagingapp.utils.FileType
import kotlinx.parcelize.Parcelize

@Parcelize
data class FileData(
    val uri: Uri? = null,
    val type: FileType? = null,
    val fileName: String? = null
) : Parcelable