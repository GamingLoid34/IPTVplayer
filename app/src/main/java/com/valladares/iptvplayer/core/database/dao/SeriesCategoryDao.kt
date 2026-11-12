package com.valladares.iptvplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.valladares.iptvplayer.core.database.entity.SeriesCategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access for Xtream series categories.
 */
@Dao
interface SeriesCategoryDao {
    /**
     * Observes series categories for a playlist.
     */
    @Query("SELECT * FROM series_categories WHERE playlistId = :playlistId ORDER BY id ASC")
    fun observeByPlaylistId(playlistId: String): Flow<List<SeriesCategoryEntity>>

    /**
     * Inserts or replaces series categories.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<SeriesCategoryEntity>)

    /**
     * Deletes all series categories for a playlist.
     */
    @Query("DELETE FROM series_categories WHERE playlistId = :playlistId")
    suspend fun deleteByPlaylistId(playlistId: String)

    /**
     * Replaces all series categories for a playlist.
     */
    @Transaction
    suspend fun replaceForPlaylist(playlistId: String, categories: List<SeriesCategoryEntity>) {
        deleteByPlaylistId(playlistId)
        insertAll(categories)
    }
}
