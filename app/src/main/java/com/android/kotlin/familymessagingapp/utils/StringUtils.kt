package com.android.kotlin.familymessagingapp.utils

import android.graphics.Typeface
import android.os.Build
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.model.ChatActivityType
import com.android.kotlin.familymessagingapp.model.ChatRoom
import com.android.kotlin.familymessagingapp.model.FileType
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

private const val emailRegex =
    "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$"

private fun chatRoomIDGenerator(uid1: String, uid2: String): String = "${uid1}___${uid2}"

object StringUtils {
    fun isValidEmail(email: String): Boolean {
        return Pattern.compile(emailRegex).matcher(email).matches()
    }

    fun isNumber(str: String): Boolean = str.all { it.isDigit() }

    fun isValidPasswordLength(password: String): Boolean = password.length >= 6

    fun generateBearerToken(token: String): String = "Bearer $token"

    fun convertFromBearerTokenToString(bearerToken: String): String = bearerToken.split(" ")[1]

    fun isValidFirebasePath(path: String): Boolean {
        return !path.contains(".")
                && !path.contains("#")
                && !path.contains("$")
                && !path.contains("[")
                && !path.contains("]")
    }

    fun generateChatRoomId(uid1: String, uid2: String): String = chatRoomIDGenerator(uid1, uid2)

    fun getCurrentTime(): Long = System.currentTimeMillis()

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatTime(milliseconds: Long, isChatRoomFormat: Boolean): String {
        val now = LocalDate.now()
        val dateTime =
            Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val date = dateTime.toLocalDate()

        return when {
            date.isEqual(now) -> dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            date.year == now.year -> dateTime.format(DateTimeFormatter.ofPattern(if (isChatRoomFormat) "d MMM" else "HH:mm d/MM"))
            else -> dateTime.format(DateTimeFormatter.ofPattern(if (isChatRoomFormat) "d MMM yyyy" else "HH:mm d/MM/yyyy"))
        }
    }

    fun isValidUrl(url: String): Boolean {
        val urlPattern = "^(http|https|ftp)://[a-zA-Z0-9\\-._~:/?#[\\]@!$&'()*+,;=%]+$"
        val regex = Regex(urlPattern)
        return regex.matches(url)
    }

    fun getFormattedMessageOfLatestActivityInChatRoom(textView: TextView, chatRoom: ChatRoom) {
        val context = textView.context
        var result = context.getString(R.string.connected)
        val chatRoomActivity = chatRoom.chatRoomActivity

        var isNewMessageActivity = false
        if (chatRoomActivity != null) {
            when (chatRoomActivity.activityType) {
                ChatActivityType.NEW_MESSAGE.value -> {
                    isNewMessageActivity = true
                    val message = chatRoomActivity.newMessage ?: return
                    val text = message.text
                    val medias = message.medias

                    if (!text.isNullOrEmpty()) {
                        result = text
                    } else if (!medias.isNullOrEmpty() && text.isNullOrEmpty()) {

                        // Check all medias are images
                        val isAllImage = medias.all { it.type == FileType.IMAGE.value }

                        result =
                            context.getString(if (isAllImage) R.string.photo_last_message else R.string.sent_file)
                    }

                    // Add sender name at the beginning
                    val senderName = chatRoomActivity.dataOfUserPerformingTheActivity?.username
                    if (senderName != null) {
                        result = if (message.senderId == Firebase.auth.uid) {
                            context.getString(R.string.sender_you) + ": $result"
                        } else {
                            "$senderName: $result"
                        }
                    }
                }

                ChatActivityType.LEAVE_CHATROOM.value -> {

                }

                ChatActivityType.PIN_MESSAGE.value -> {

                }

                ChatActivityType.REMOVE_MESSAGE.value -> {
                    val userdata = chatRoomActivity.dataOfUserPerformingTheActivity
                    val senderName =
                        if (userdata?.uid == Firebase.auth.uid) context.getString(R.string.sender_you) else userdata?.username
                    result = context.getString(R.string.user_deleted_a_message, senderName)
                }

                ChatActivityType.JOIN_CHATROOM.value -> {

                }

                ChatActivityType.UPDATE_CHATROOM_INFO.value -> {

                }

                ChatActivityType.CREATE_CHATROOM.value -> {
                    val senderName = chatRoomActivity.dataOfUserPerformingTheActivity?.username
                    if (senderName != null) {
                        result = context.getString(
                            R.string.user_created_group_chat,
                            if (chatRoomActivity.performedByUser == Firebase.auth.uid) context.getString(
                                R.string.sender_you
                            ) else senderName
                        )
                    }
                }

                else -> {

                }
            }

        }
        textView.text = result
        textView.setTypeface(
            null,
            if (isNewMessageActivity) Typeface.NORMAL else Typeface.ITALIC
        )
    }

    fun areListsEqual(list1: List<String>, list2: List<String>): Boolean {
        return list1.toTypedArray().contentEquals(list2.toTypedArray())
    }

    fun generateFormattedQrCode(text: String): String = "${text}___${text}___${text}"

    fun isValidQrCode(text: String): Boolean {
        val uids = text.split("___")
        return if (uids.size == 3) {
            return uids[0] == uids[1] && uids[1] == uids[2]
        } else {
            false
        }
    }

    fun getUidFromFormattedQrCode(text: String): String = text.split("___")[0]

    fun formatMyQRCodeImageName(username: String?): String {
        var formattedName = username
        formattedName =
            if (formattedName.isNullOrEmpty()) Constant.MY_QR_CODE_DEFAULT_NAME else formattedName.replace(
                " ",
                "_"
            )
        return formattedName + "_" + getCurrentTime()
    }
}
