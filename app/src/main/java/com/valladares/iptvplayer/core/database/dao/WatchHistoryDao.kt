package com.valladares.iptvplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.valladares.iptvplayer.core.database.entity.LiveChannelEntity
import com.valladares.iptvplayer.core.database.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access for watch history entries.
 */
@Dao
interface WatchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(entity: WatchHistoryEntity)

    @Query(
        """
        SELECT * FROM watch_history w1
        WHERE playlistId = :playlistId
        AND startedAt = (
            SELECT MAX(startedAt) FROM watch_history w2
            WHERE w2.liveChannelId = w1.liveChannelId
            AND w2.playlistId = w1.playlistId
        )
        ORDER BY startedAt DESC
        LIMIT 20
        """
    )
    fun observeRecentForPlaylist(playlistId: String): Flow<List<WatchHistoryEntity>>

    @Query(
        """
        SELECT lc.* FROM live_channels lc
        INNER JOIN (
            SELECT liveChannelId, MAX(startedAt) AS latestStartedAt
            FROM watch_history
            WHERE playlistId = :playlistId
            GROUP BY liveChannelId
        ) h ON h.liveChannelId = lc.id
        WHERE lc.playlistId = :playlistId
        ORDER BY h.latestStartedAt DESC
        LIMIT 20
        """
    )
    fun observeRecentChannels(playlistId: String): Flow<List<LiveChannelEntity>>

    @Query(
        """
        DELETE FROM watch_history
        WHERE playlistId = :playlistId
        AND id NOT IN (
            SELECT id FROM watch_history
            WHERE playlistId = :playlistId
            ORDER BY startedAt DESC
            LIMIT 100
        )
        """
    )
    suspend fun trimHistory(playlistId: String)
}
