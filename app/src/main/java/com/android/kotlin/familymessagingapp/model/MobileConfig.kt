package com.android.kotlin.familymessagingapp.model

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@Parcelize
@IgnoreExtraProperties
data class MobileConfig(
    val turnOnSuggestedAnswers: Boolean? = null
) : Parcelable {
    companion object {
        const val TURN_ON_SUGGESTED_ANSWERS = "turnOnSuggestedAnswers"
    }
}