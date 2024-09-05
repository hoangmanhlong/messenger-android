package com.android.kotlin.familymessagingapp.model

import android.os.Parcelable
import com.google.firebase.database.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatRoomActivity(
    val latestActiveTime: Long? = null,
    val activityType: String? = null,
    val performedByUser: String? = null,
    val newMessage: Message? = null,
    @Exclude val dataOfUserPerformingTheActivity: UserData? = null
) : Parcelable

enum class ChatActivityType(val value: String) {
    NEW_MESSAGE("0"),
    LEAVE_CHATROOM("1"),
    PIN_MESSAGE("2"),
    REMOVE_MESSAGE("3"),
    JOIN_CHATROOM("4"),
    UPDATE_CHATROOM_NAME("5"),
    UPDATE_CHATROOM_IMAGE("6"),
    UPDATE_CHATROOM_INFO("7"),
    CREATE_CHATROOM("8"),
}