package com.valladares.iptvplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.valladares.iptvplayer.core.database.entity.ChannelEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access for [ChannelEntity] rows.
 */
@Dao
interface ChannelDao {
    /**
     * Inserts channel rows. Uses the default on-conflict strategy (ABORT); full playlist
     * replacement is done via [replaceChannelsForPlaylist].
     */
    @Insert
    suspend fun insertAll(channels: List<ChannelEntity>)

    /**
     * Removes all channels for the given [playlistId].
     */
    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun deleteByPlaylistId(playlistId: String)

    /**
     * Observes channels for a playlist in M3U order.
     */
    @Query("SELECT * FROM channels WHERE playlistId = :playlistId ORDER BY sortOrder ASC")
    fun observeByPlaylistId(playlistId: String): Flow<List<ChannelEntity>>

    /**
     * Returns a channel by internal primary key, or null if missing.
     */
    @Query("SELECT * FROM channels WHERE id = :id")
    suspend fun getById(id: Long): ChannelEntity?

    /**
     * Replaces the full channel set for a playlist in a single transaction.
     */
    @Transaction
    suspend fun replaceChannelsForPlaylist(playlistId: String, channels: List<ChannelEntity>) {
        deleteByPlaylistId(playlistId)
        insertAll(channels)
    }
}
