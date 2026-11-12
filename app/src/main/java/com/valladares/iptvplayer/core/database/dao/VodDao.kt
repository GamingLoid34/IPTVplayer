package com.valladares.iptvplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.valladares.iptvplayer.core.database.entity.VodEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access for Xtream VOD items.
 */
@Dao
interface VodDao {
    /**
     * Observes all VOD items for a playlist.
     */
    @Query("SELECT * FROM vod_items WHERE playlistId = :playlistId ORDER BY sortOrder ASC")
    fun observeByPlaylistId(playlistId: String): Flow<List<VodEntity>>

    /**
     * Observes VOD items filtered by category (or uncategorized when null).
     */
    @Query(
        """
        SELECT * FROM vod_items
        WHERE playlistId = :playlistId
        AND ((:categoryExternalId IS NULL AND categoryExternalId IS NULL) OR categoryExternalId = :categoryExternalId)
        ORDER BY sortOrder ASC
        """
    )
    fun observeByPlaylistAndCategory(
        playlistId: String,
        categoryExternalId: String?
    ): Flow<List<VodEntity>>

    /**
     * Returns one VOD item by row id.
     */
    @Query("SELECT * FROM vod_items WHERE id = :id")
    suspend fun getById(id: Long): VodEntity?

    /**
     * Inserts or replaces VOD rows.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<VodEntity>)

    /**
     * Deletes all VOD rows for a playlist.
     */
    @Query("DELETE FROM vod_items WHERE playlistId = :playlistId")
    suspend fun deleteByPlaylistId(playlistId: String)

    /**
     * Replaces all VOD rows for a playlist.
     */
    @Transaction
    suspend fun replaceForPlaylist(playlistId: String, items: List<VodEntity>) {
        deleteByPlaylistId(playlistId)
        insertAll(items)
    }
}
