package com.android.kotlin.familymessagingapp.model

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@Parcelize
@IgnoreExtraProperties
data class PrivateUserData(
    val chatRooms: List<String>? = null,
    val mobileConfig: MobileConfig? = null,
    val searchHistories: Map<String, SearchHistory>? = null,
    val contacts: Map<String, ContactDto>? = null
): Parcelable {
    companion object {
        const val CHAT_ROOMS = "chatRooms"
        const val MOBILE_CONFIG = "mobileConfig"
        const val SEARCH_HISTORIES = "searchHistories"
    }
}
