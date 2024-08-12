package com.android.kotlin.familymessagingapp.model

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@Parcelize
@IgnoreExtraProperties
data class PrivateUserData(
    val chatRooms: List<String>? = null,
    val mobileConfig: MobileConfig? = null
): Parcelable
