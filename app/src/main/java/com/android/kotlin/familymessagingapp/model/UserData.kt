package com.android.kotlin.familymessagingapp.model

import android.os.Parcelable
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

/**
 * Bug:
 * UserData does not define a no-argument constructor. If you are using ProGuard, make sure these constructors are not stripped.
 * Solution: init properties = null
 */


@Parcelize
@IgnoreExtraProperties
data class UserData(
    val uid: String? = null,
    val username: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val userAvatar: String? = null,
) : Parcelable {
    companion object {
        const val USERNAME = "username"
        const val PHONE_NUMBER = "phoneNumber"
        const val EMAIL = "email"
    }
}

fun FirebaseUser.toUserData(): UserData = UserData(
    uid = uid,
    username = displayName,
    userAvatar = photoUrl?.toString(),
    email = email,
    phoneNumber = phoneNumber
)

