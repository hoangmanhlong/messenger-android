package com.android.kotlin.familymessagingapp.utils

import java.util.regex.Pattern

object RegexUtils {
    fun isValidEmail(email: String): Boolean {
        val emailRegex = ("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")
        return Pattern.compile(emailRegex).matcher(email).matches()
    }

    fun isValidPasswordLength(password: String): Boolean = password.length >= 6
}
