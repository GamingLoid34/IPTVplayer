package com.valladares.iptvplayer.data.xtream

import com.valladares.iptvplayer.core.database.dao.WatchHistoryDao
import com.valladares.iptvplayer.core.database.entity.WatchHistoryEntity
import com.valladares.iptvplayer.core.database.mapper.toDomain
import com.valladares.iptvplayer.data.xtream.model.LiveChannel
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed watch history repository.
 */
@Singleton
class WatchHistoryRepositoryImpl @Inject constructor(
    private val watchHistoryDao: WatchHistoryDao
) : WatchHistoryRepository {
    override suspend fun recordWatch(playlistId: String, liveChannelId: Long, durationMs: Long) {
        if (durationMs < 30_000L) {
            return
        }
        watchHistoryDao.add(
            WatchHistoryEntity(
                playlistId = playlistId,
                liveChannelId = liveChannelId,
                startedAt = System.currentTimeMillis(),
                durationMs = durationMs
            )
        )
        watchHistoryDao.trimHistory(playlistId)
    }

    override fun observeRecentChannels(playlistId: String): Flow<List<LiveChannel>> =
        watchHistoryDao.observeRecentChannels(playlistId).map { rows ->
            rows.map { it.toDomain() }
        }
}
