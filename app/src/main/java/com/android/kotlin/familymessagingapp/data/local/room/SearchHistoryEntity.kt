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
data class SearchHistoryEntity(@PrimaryKey val text: String, val time: Long)

@Dao
@Fts4 // FullTextSearch
interface SearchHistoryDao {

    @Query("SELECT * FROM search_history ORDER BY time DESC")
    fun getSearchHistories(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSearchHistory(searchHistoryEntity: SearchHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchHistoryFromServer(vararg searchHistoryEntity: SearchHistoryEntity)

    @Delete
    suspend fun deleteSearchHistory(searchHistoryEntity: SearchHistoryEntity)

    @Query("DELETE FROM SEARCH_HISTORY")
    suspend fun deleteAllSearchHistories()

    @Query(" SELECT * FROM search_history WHERE text LIKE  '%' || :query || '%'")
    suspend fun searchByTitle(query: String): List<SearchHistoryEntity>?
}