package com.android.kotlin.familymessagingapp.data.local.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "search_history")
data class SearchHistory(@PrimaryKey val text: String, val time: Long)

@Dao
@Fts4 // FullTextSearch
interface SearchHistoryDao {

    @Query("SELECT * FROM search_history ORDER BY time DESC")
    fun getSearchHistories(): Flow<List<SearchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSearchHistory(searchHistory: SearchHistory)

    @Delete
    suspend fun deleteSearchHistory(searchHistory: SearchHistory)

    @Query("DELETE FROM SEARCH_HISTORY")
    suspend fun deleteAllSearchHistories()

    @Query(" SELECT * FROM search_history WHERE text LIKE  '%' || :query || '%'")
    suspend fun searchByTitle(query: String): List<SearchHistory>?
}