package com.valladares.iptvplayer.data.xtream

import com.valladares.iptvplayer.data.xtream.model.VodItem
import com.valladares.iptvplayer.data.xtream.model.XtreamCategory
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for Xtream VOD categories and items.
 */
interface VodRepository {
    /**
     * Observes VOD categories for a playlist.
     */
    fun observeCategories(playlistId: String): Flow<List<XtreamCategory>>

    /**
     * Observes all VOD items for a playlist.
     */
    fun observeItems(playlistId: String): Flow<List<VodItem>>

    /**
     * Observes VOD items filtered by [categoryExternalId] (or uncategorized when null).
     */
    fun observeItemsByCategory(playlistId: String, categoryExternalId: String?): Flow<List<VodItem>>

    /**
     * Returns one VOD item by row id.
     */
    suspend fun getItem(id: Long): VodItem?
}
