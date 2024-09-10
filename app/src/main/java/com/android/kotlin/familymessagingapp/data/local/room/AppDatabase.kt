package com.android.kotlin.familymessagingapp.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@TypeConverters(Converter::class)
@Database(entities = [MessageEntity::class, SearchHistoryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val messageDao: MessageDao
    abstract val searchHistoryDao: SearchHistoryDao
}