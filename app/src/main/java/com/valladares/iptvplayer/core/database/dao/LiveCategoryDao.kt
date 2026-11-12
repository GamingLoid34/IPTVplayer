package com.valladares.iptvplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.valladares.iptvplayer.core.database.entity.LiveCategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access for live Xtream categories.
 */
@Dao
interface LiveCategoryDao {
    /**
     * Observes live categories for a playlist.
     */
    @Query("SELECT * FROM live_categories WHERE playlistId = :playlistId ORDER BY id ASC")
    fun observeByPlaylistId(playlistId: String): Flow<List<LiveCategoryEntity>>

    /**
     * Inserts or replaces live categories.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<LiveCategoryEntity>)

    /**
     * Deletes all live categories for the playlist.
     */
    @Query("DELETE FROM live_categories WHERE playlistId = :playlistId")
    suspend fun deleteByPlaylistId(playlistId: String)

    /**
     * Replaces all live categories for the playlist in one transaction.
     */
    @Transaction
    suspend fun replaceForPlaylist(playlistId: String, categories: List<LiveCategoryEntity>) {
        deleteByPlaylistId(playlistId)
        insertAll(categories)
    }
}
