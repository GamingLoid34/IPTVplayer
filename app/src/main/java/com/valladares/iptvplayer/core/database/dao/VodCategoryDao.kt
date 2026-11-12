package com.valladares.iptvplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.valladares.iptvplayer.core.database.entity.VodCategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access for Xtream VOD categories.
 */
@Dao
interface VodCategoryDao {
    /**
     * Observes VOD categories for a playlist.
     */
    @Query("SELECT * FROM vod_categories WHERE playlistId = :playlistId ORDER BY id ASC")
    fun observeByPlaylistId(playlistId: String): Flow<List<VodCategoryEntity>>

    /**
     * Inserts or replaces VOD categories.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<VodCategoryEntity>)

    /**
     * Deletes all VOD categories for the playlist.
     */
    @Query("DELETE FROM vod_categories WHERE playlistId = :playlistId")
    suspend fun deleteByPlaylistId(playlistId: String)

    /**
     * Replaces all VOD categories for a playlist.
     */
    @Transaction
    suspend fun replaceForPlaylist(playlistId: String, categories: List<VodCategoryEntity>) {
        deleteByPlaylistId(playlistId)
        insertAll(categories)
    }
}
