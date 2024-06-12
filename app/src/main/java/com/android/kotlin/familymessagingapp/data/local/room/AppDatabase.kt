package com.android.kotlin.familymessagingapp.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android.kotlin.familymessagingapp.data.local.room.entities.MessageEntity

@Database(entities = [MessageEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val dao: AppDao
}