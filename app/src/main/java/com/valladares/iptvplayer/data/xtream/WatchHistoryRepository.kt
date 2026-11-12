package com.valladares.iptvplayer.data.xtream

import com.valladares.iptvplayer.data.xtream.model.LiveChannel
import kotlinx.coroutines.flow.Flow

/**
 * Repository for persisting and reading recently watched channels.
 */
interface WatchHistoryRepository {
    suspend fun recordWatch(playlistId: String, liveChannelId: Long, durationMs: Long)
    fun observeRecentChannels(playlistId: String): Flow<List<LiveChannel>>
}
