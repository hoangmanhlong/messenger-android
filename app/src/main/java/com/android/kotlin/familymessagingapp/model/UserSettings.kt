package com.android.kotlin.familymessagingapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserSettings(
    val enabledAI: Boolean? = null,
) : Parcelable {
    companion object {
        const val ENABLED_AI = "enabledAI"
    }
}
