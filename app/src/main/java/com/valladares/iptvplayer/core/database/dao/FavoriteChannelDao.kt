package com.valladares.iptvplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.valladares.iptvplayer.core.database.entity.FavoriteChannelEntity
import com.valladares.iptvplayer.core.database.entity.LiveChannelEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access for persisted favorites.
 */
@Dao
interface FavoriteChannelDao {
    @Query("SELECT * FROM favorite_channels WHERE playlistId = :playlistId ORDER BY sortOrder ASC, addedAt DESC")
    fun observeForPlaylist(playlistId: String): Flow<List<FavoriteChannelEntity>>

    @Query("SELECT liveChannelId FROM favorite_channels WHERE playlistId = :playlistId")
    fun observeFavoriteIds(playlistId: String): Flow<List<Long>>

    @Query(
        """
        SELECT lc.* FROM live_channels lc
        INNER JOIN favorite_channels f ON f.liveChannelId = lc.id
        WHERE f.playlistId = :playlistId
        ORDER BY f.sortOrder ASC, f.addedAt DESC
        """
    )
    fun observeFavoriteChannels(playlistId: String): Flow<List<LiveChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(entity: FavoriteChannelEntity)

    @Query("DELETE FROM favorite_channels WHERE playlistId = :playlistId AND liveChannelId = :liveChannelId")
    suspend fun remove(playlistId: String, liveChannelId: Long)

    @Query("SELECT COUNT(*) FROM favorite_channels WHERE playlistId = :playlistId AND liveChannelId = :liveChannelId")
    suspend fun isFavorite(playlistId: String, liveChannelId: Long): Int
}
