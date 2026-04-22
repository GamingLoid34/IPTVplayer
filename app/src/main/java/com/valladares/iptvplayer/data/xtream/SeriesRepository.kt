package com.valladares.iptvplayer.data.xtream

import com.valladares.iptvplayer.data.xtream.model.Episode
import com.valladares.iptvplayer.data.xtream.model.Series
import com.valladares.iptvplayer.data.xtream.model.XtreamCategory
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for Xtream series, categories and episodes.
 */
interface SeriesRepository {
    /**
     * Observes series categories for a playlist.
     */
    fun observeCategories(playlistId: String): Flow<List<XtreamCategory>>

    /**
     * Observes series list for a playlist.
     */
    fun observeSeriesList(playlistId: String): Flow<List<Series>>

    /**
     * Observes series list filtered by [categoryExternalId] (or uncategorized when null).
     */
    fun observeSeriesByCategory(playlistId: String, categoryExternalId: String?): Flow<List<Series>>

    /**
     * Observes episodes for one series row id.
     */
    fun observeEpisodes(seriesRowId: Long): Flow<List<Episode>>

    /**
     * Returns one series row by id.
     */
    suspend fun getSeries(id: Long): Series?

    /**
     * Returns one episode by id.
     */
    suspend fun getEpisode(id: Long): Episode?
}
