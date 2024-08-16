package com.android.kotlin.familymessagingapp.model

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@Parcelize
@IgnoreExtraProperties
data class ContactDto(
    val uid: String? = null,
    val status: String? = null
) : Parcelable

data class Contact(
    val uid: String? = null,
    val status: String? = null,
    val contactData: UserData? = null
)

fun ContactDto.toContact(): Contact = Contact(uid = uid, status = status)

enum class ContactStatus(value:  String) {
    ACTIVE(value = "0"),
    BLOCKED(value = "1")
}
