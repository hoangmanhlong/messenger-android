package com.android.kotlin.familymessagingapp.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FileData(
    val uri: Uri? = null,
    val type: FileType? = null,
    val fileName: String? = null,
    val fileSize: String? = null,
    val mime: String? = null
) : Parcelable