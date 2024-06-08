package com.android.kotlin.familymessagingapp.data.remote

import com.android.kotlin.familymessagingapp.data.local.room.AppDatabase

class MessageRemoteMediator(
    private val query: String,
    private val database: AppDatabase,
    private val networkService: AppDatabase
) {
}