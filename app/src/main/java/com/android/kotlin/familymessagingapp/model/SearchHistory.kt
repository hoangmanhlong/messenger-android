package com.android.kotlin.familymessagingapp.model

import android.os.Parcelable
import com.android.kotlin.familymessagingapp.data.local.room.SearchHistoryEntity
import com.google.firebase.database.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
class SearchHistory(
    val text: String? = null,
    val latestSearchTime: Long? = null
) : Parcelable {

    @Exclude
    fun toSearchHistoryEntity(): SearchHistoryEntity = SearchHistoryEntity(
        text = text ?: "",
        time = latestSearchTime ?: 0L
    )
}