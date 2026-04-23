package com.valladares.iptvplayer.data.xtream

import com.valladares.iptvplayer.data.xtream.model.LiveChannel
import kotlinx.coroutines.flow.Flow

/**
 * Repository for playlist favorites.
 */
interface FavoritesRepository {
    fun observeFavoriteIds(playlistId: String): Flow<Set<Long>>
    fun observeFavorites(playlistId: String): Flow<List<LiveChannel>>
    suspend fun addFavorite(playlistId: String, liveChannelId: Long)
    suspend fun removeFavorite(playlistId: String, liveChannelId: Long)
    suspend fun toggleFavorite(playlistId: String, liveChannelId: Long)
}
