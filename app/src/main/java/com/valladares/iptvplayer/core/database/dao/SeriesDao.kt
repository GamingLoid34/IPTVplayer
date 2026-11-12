package com.valladares.iptvplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.valladares.iptvplayer.core.database.entity.SeriesEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access for Xtream series rows.
 */
@Dao
interface SeriesDao {
    /**
     * Observes series list for a playlist.
     */
    @Query("SELECT * FROM series WHERE playlistId = :playlistId ORDER BY sortOrder ASC")
    fun observeByPlaylistId(playlistId: String): Flow<List<SeriesEntity>>

    /**
     * Observes series rows filtered by category (or uncategorized when null).
     */
    @Query(
        """
        SELECT * FROM series
        WHERE playlistId = :playlistId
        AND ((:categoryExternalId IS NULL AND categoryExternalId IS NULL) OR categoryExternalId = :categoryExternalId)
        ORDER BY sortOrder ASC
        """
    )
    fun observeByPlaylistAndCategory(
        playlistId: String,
        categoryExternalId: String?
    ): Flow<List<SeriesEntity>>

    /**
     * Returns a single series row by id.
     */
    @Query("SELECT * FROM series WHERE id = :id")
    suspend fun getById(id: Long): SeriesEntity?

    /**
     * Inserts or replaces series rows.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(series: List<SeriesEntity>)

    /**
     * Deletes all series rows for a playlist.
     */
    @Query("DELETE FROM series WHERE playlistId = :playlistId")
    suspend fun deleteByPlaylistId(playlistId: String)

    /**
     * Replaces all series rows for a playlist.
     */
    @Transaction
    suspend fun replaceForPlaylist(playlistId: String, series: List<SeriesEntity>) {
        deleteByPlaylistId(playlistId)
        insertAll(series)
    }
}
