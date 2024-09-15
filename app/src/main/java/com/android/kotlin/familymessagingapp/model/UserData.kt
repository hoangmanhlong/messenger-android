package com.android.kotlin.familymessagingapp.model

import android.os.Parcelable
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
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
    val uid: String? = null,
    val username: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val userAvatar: String? = null,
) : Parcelable {
    companion object {
        const val UID = "uid"
        const val USERNAME = "username"
        const val PHONE_NUMBER = "phoneNumber"
        const val EMAIL = "email"
    }

    @Exclude
    fun verified(): Boolean = !this.email.isNullOrEmpty()
            && !this.phoneNumber.isNullOrEmpty()
            && !this.username.isNullOrEmpty()
            && !this.uid.isNullOrEmpty()

    @Exclude
    fun getShortcutId(): String = "shortcut_$uid"
}

fun FirebaseUser.toUserData(): UserData = UserData(
    uid = uid,
    username = displayName,
    userAvatar = photoUrl?.toString(),
    email = email,
    phoneNumber = phoneNumber
)

