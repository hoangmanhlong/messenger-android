package com.android.kotlin.familymessagingapp.data.remote.dto.res

import java.util.Objects

data class ObjectResponse(
    val status: Int?,
    val data: Objects?,
    val message: String?
)
