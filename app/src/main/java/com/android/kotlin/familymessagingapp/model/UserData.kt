package com.android.kotlin.familymessagingapp.model

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@IgnoreExtraProperties
data class UserData(
    @Expose @SerializedName("uid") val uid: String,
    @Expose @SerializedName("username") val username: String?,
    @Expose @SerializedName("email") val email: String?,
    @Expose @SerializedName("phone_number") val phoneNumber: String?,
    @Expose @SerializedName("user_avatar") val userAvatar: String?
) : Parcelable
