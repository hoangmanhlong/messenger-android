package com.android.kotlin.familymessagingapp.model

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

// Bug:
// UserData does not define a no-argument constructor. If you are using ProGuard, make sure these constructors are not stripped.
// Solution: init properties = null
/**
 * User data
 *
 * @property uid user identifier
 * @property username User name
 * @property chatrooms chatroom identifier list of user
 */
@Parcelize
@IgnoreExtraProperties
data class UserData(
    @Expose @SerializedName(UID) val uid: String? = null,
    @Expose @SerializedName(USERNAME) val username: String? = null,
//    @Expose @SerializedName("username_lowercase") val usernameLowercase: String? = null,
    @Expose @SerializedName(EMAIL) val email: String? = null,
    @Expose @SerializedName(PHONE_NUMBER) val phoneNumber: String? = null,
    @Expose @SerializedName("user_avatar") val userAvatar: String? = null,
    @Expose @SerializedName(CHAT_ROOMS) val chatrooms: List<String>? = null
) : Parcelable {
    companion object {
        const val UID = "uid"
        const val USERNAME = "username"
        const val USERNAME_LOWERCASE = "usernameLowercase"
        const val PHONE_NUMBER = "phoneNumber"
        const val EMAIL = "email"
        const val CHAT_ROOMS = "chatrooms"
    }
}

