package com.android.kotlin.familymessagingapp.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.android.kotlin.familymessagingapp.R
import com.android.kotlin.familymessagingapp.model.Message
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

    fun showLastMessageToChatRoomView(context: Context, message: Message?): String {
        var result = context.getString(R.string.connected)
        if (message != null) {
            result = if (!message.text.isNullOrEmpty() || !message.photo.isNullOrEmpty()) {
                if (!message.text.isNullOrEmpty()) message.text else context.getString(R.string.photo_last_message)
            } else {
                result
            }
        }
        return result
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

    fun getUidFromFormattedQrCode(text: String): String =text.split("___")[0]
}
