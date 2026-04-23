package com.valladares.iptvplayer.data.xtream

import com.valladares.iptvplayer.data.xtream.model.LiveChannel
import com.valladares.iptvplayer.data.xtream.model.XtreamCategory
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for Xtream live categories and channels.
 */
interface LiveRepository {
    /**
     * Observes live categories for a playlist.
     */
    fun observeCategories(playlistId: String): Flow<List<XtreamCategory>>

    /**
     * Observes all live channels for a playlist.
     */
    fun observeChannels(playlistId: String): Flow<List<LiveChannel>>

    /**
     * Observes available detected country codes for channels in the playlist.
     */
    fun observeAvailableCountries(playlistId: String): Flow<List<String>>

    /**
     * Observes channels filtered by optional country and search query.
     */
    fun observeChannelsFiltered(
        playlistId: String,
        countryCode: String?,
        searchQuery: String?
    ): Flow<List<LiveChannel>>

    /**
     * Observes channels filtered by [categoryExternalId] (or uncategorized when null).
     */
    fun observeChannelsByCategory(
        playlistId: String,
        categoryExternalId: String?
    ): Flow<List<LiveChannel>>

    /**
     * Returns a single live channel by row id.
     */
    suspend fun getChannel(id: Long): LiveChannel?
}
