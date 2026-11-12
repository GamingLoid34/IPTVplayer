package com.valladares.iptvplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.valladares.iptvplayer.core.database.entity.LiveChannelEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access for Xtream live channels.
 */
@Dao
interface LiveChannelDao {
    /**
     * Observes all live channels for a playlist.
     */
    @Query("SELECT * FROM live_channels WHERE playlistId = :playlistId ORDER BY sortOrder ASC")
    fun observeByPlaylistId(playlistId: String): Flow<List<LiveChannelEntity>>

    /**
     * Observes distinct non-null country codes available in playlist channels.
     */
    @Query(
        """
        SELECT DISTINCT detectedCountryCode
        FROM live_channels
        WHERE playlistId = :playlistId
        AND detectedCountryCode IS NOT NULL
        ORDER BY detectedCountryCode
        """
    )
    fun observeAvailableCountries(playlistId: String): Flow<List<String>>

    /**
     * Observes live channels filtered by category (or uncategorized when null).
     */
    @Query(
        """
        SELECT * FROM live_channels
        WHERE playlistId = :playlistId
        AND ((:categoryExternalId IS NULL AND categoryExternalId IS NULL) OR categoryExternalId = :categoryExternalId)
        ORDER BY sortOrder ASC
        """
    )
    fun observeByPlaylistAndCategory(
        playlistId: String,
        categoryExternalId: String?
    ): Flow<List<LiveChannelEntity>>

    /**
     * Observes channels filtered by optional country and search query.
     */
    @Query(
        """
        SELECT * FROM live_channels
        WHERE playlistId = :playlistId
        AND (
            :countryCode IS NULL
            OR (:countryCode = 'UNKNOWN' AND detectedCountryCode IS NULL)
            OR detectedCountryCode = :countryCode
        )
        AND (
            :searchQuery IS NULL
            OR TRIM(:searchQuery) = ''
            OR UPPER(name) LIKE '%' || UPPER(:searchQuery) || '%'
        )
        ORDER BY sortOrder ASC
        """
    )
    fun observeChannelsFiltered(
        playlistId: String,
        countryCode: String?,
        searchQuery: String?
    ): Flow<List<LiveChannelEntity>>

    /**
     * Returns one channel by row id.
     */
    @Query("SELECT * FROM live_channels WHERE id = :id")
    suspend fun getById(id: Long): LiveChannelEntity?

    /**
     * Inserts or replaces live channels.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(channels: List<LiveChannelEntity>)

    /**
     * Deletes all live channels for a playlist.
     */
    @Query("DELETE FROM live_channels WHERE playlistId = :playlistId")
    suspend fun deleteByPlaylistId(playlistId: String)

    /**
     * Replaces all live channels for the playlist.
     */
    @Transaction
    suspend fun replaceForPlaylist(playlistId: String, channels: List<LiveChannelEntity>) {
        deleteByPlaylistId(playlistId)
        insertAll(channels)
    }
}
