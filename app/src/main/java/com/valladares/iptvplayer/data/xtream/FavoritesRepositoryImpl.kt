package com.valladares.iptvplayer.data.xtream

import com.valladares.iptvplayer.core.database.dao.FavoriteChannelDao
import com.valladares.iptvplayer.core.database.entity.FavoriteChannelEntity
import com.valladares.iptvplayer.core.database.mapper.toDomain
import com.valladares.iptvplayer.data.xtream.model.LiveChannel
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed favorites repository.
 */
@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val favoriteChannelDao: FavoriteChannelDao
) : FavoritesRepository {
    override fun observeFavoriteIds(playlistId: String): Flow<Set<Long>> =
        favoriteChannelDao.observeFavoriteIds(playlistId).map { it.toSet() }

    override fun observeFavorites(playlistId: String): Flow<List<LiveChannel>> =
        favoriteChannelDao.observeFavoriteChannels(playlistId).map { rows ->
            rows.map { it.toDomain() }
        }

    override suspend fun addFavorite(playlistId: String, liveChannelId: Long) {
        favoriteChannelDao.add(
            FavoriteChannelEntity(
                playlistId = playlistId,
                liveChannelId = liveChannelId
            )
        )
    }

    override suspend fun removeFavorite(playlistId: String, liveChannelId: Long) {
        favoriteChannelDao.remove(playlistId, liveChannelId)
    }

    override suspend fun toggleFavorite(playlistId: String, liveChannelId: Long) {
        val isFavorite = favoriteChannelDao.isFavorite(playlistId, liveChannelId) > 0
        if (isFavorite) {
            favoriteChannelDao.remove(playlistId, liveChannelId)
        } else {
            addFavorite(playlistId, liveChannelId)
        }
    }
}
