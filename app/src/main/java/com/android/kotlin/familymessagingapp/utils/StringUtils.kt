package com.android.kotlin.familymessagingapp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.regex.Pattern

object StringUtils {
    fun isValidEmail(email: String): Boolean {
        val emailRegex =
            ("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")
        return Pattern.compile(emailRegex).matcher(email).matches()
    }

    fun isNumber(string: String): Boolean {
        val pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        return pattern.matcher(string).matches();
    }

    fun isValidPasswordLength(password: String): Boolean = password.length >= 6

    fun generateBearerToken(token: String): String = "Bearer $token"

    fun convertFromBearerTokenToString(bearerToken: String): String =
        bearerToken.split(" ")[1]

    fun composeEmail(context: Context, addresses: Array<String>, subject: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Only email apps handle this.
            putExtra(Intent.EXTRA_EMAIL, addresses)
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        intent.resolveActivity(context.packageManager).let { context.startActivity(intent) }
    }

    fun isValidFirebasePath(path: String): Boolean {
        return !path.contains(".")
                && !path.contains("#")
                && !path.contains("$")
                && !path.contains("[")
                && !path.contains("]")
    }

    fun timeFormatter(time: Long): String {
        return SimpleDateFormat("h:mm a - dd/MM/yyyy", Locale.getDefault()).format(time)
    }

    fun generateChatRoomId(uid1: String, uid2: String): String {
        return uid1 + uid2 + System.currentTimeMillis()
    }

    fun getCurrentTime(): Long = System.currentTimeMillis()

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatTime(milliseconds: Long): String {
        val now = LocalDate.now()
        val dateTime = Instant.ofEpochMilli(milliseconds).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val date = dateTime.toLocalDate()

        return when {
            date.isEqual(now) -> dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            date.year == now.year -> date.format(DateTimeFormatter.ofPattern("d MMM"))
            else -> date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
        }
    }
}
