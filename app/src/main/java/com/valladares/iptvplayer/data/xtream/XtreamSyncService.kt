package com.valladares.iptvplayer.data.xtream

import com.valladares.iptvplayer.core.database.dao.EpisodeDao
import com.valladares.iptvplayer.core.database.dao.LiveCategoryDao
import com.valladares.iptvplayer.core.database.dao.LiveChannelDao
import com.valladares.iptvplayer.core.database.dao.SeriesCategoryDao
import com.valladares.iptvplayer.core.database.dao.SeriesDao
import com.valladares.iptvplayer.core.database.dao.VodCategoryDao
import com.valladares.iptvplayer.core.database.dao.VodDao
import com.valladares.iptvplayer.core.database.mapper.toEntity
import com.valladares.iptvplayer.core.database.mapper.toLiveCategoryEntity
import com.valladares.iptvplayer.core.database.mapper.toSeriesCategoryEntity
import com.valladares.iptvplayer.core.database.mapper.toVodCategoryEntity
import com.valladares.iptvplayer.data.xtream.api.XtreamApiFactory
import com.valladares.iptvplayer.data.xtream.model.XtreamCredentials
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * Synchronizes Xtream content into Room using destructive per-playlist replacement.
 *
 * Each sync call replaces all stored rows for the relevant content type under [playlistId].
 * This strategy is used when a user imports or refreshes Xtream data.
 */
@Singleton
class XtreamSyncService @Inject constructor(
    private val apiFactory: XtreamApiFactory,
    private val liveChannelDao: LiveChannelDao,
    private val liveCategoryDao: LiveCategoryDao,
    private val vodDao: VodDao,
    private val vodCategoryDao: VodCategoryDao,
    private val seriesDao: SeriesDao,
    private val seriesCategoryDao: SeriesCategoryDao,
    private val episodeDao: EpisodeDao
) {
    /**
     * Fetches and replaces all live categories/channels for [playlistId].
     */
    suspend fun syncLive(playlistId: String, credentials: XtreamCredentials): Result<Unit> =
        withContext(Dispatchers.IO) {
            runSync("Kunde inte synka live-innehåll") {
                val api = apiFactory.create(credentials.serverUrl)
                val categories = api.getLiveCategories(
                    username = credentials.username,
                    password = credentials.password
                ).map { it.toLiveCategoryEntity(playlistId) }
                val categoriesById: Map<String, String> = categories.associate { it.externalId to it.name }
                val channels = api.getLiveStreams(
                    username = credentials.username,
                    password = credentials.password
                ).mapIndexed { index, dto ->
                    dto.toEntity(
                        playlistId = playlistId,
                        sortOrder = index,
                        categoryName = categoriesById[dto.categoryId]
                    )
                }
                liveCategoryDao.replaceForPlaylist(playlistId, categories)
                liveChannelDao.replaceForPlaylist(playlistId, channels)
            }
        }

    /**
     * Fetches and replaces all VOD categories/items for [playlistId].
     */
    suspend fun syncVod(playlistId: String, credentials: XtreamCredentials): Result<Unit> =
        withContext(Dispatchers.IO) {
            runSync("Kunde inte synka VOD-innehåll") {
                val api = apiFactory.create(credentials.serverUrl)
                val categories = api.getVodCategories(
                    username = credentials.username,
                    password = credentials.password
                ).map { it.toVodCategoryEntity(playlistId) }
                val items = api.getVodStreams(
                    username = credentials.username,
                    password = credentials.password
                ).mapIndexed { index, dto ->
                    dto.toEntity(playlistId, index)
                }.filterNotNull()
                vodCategoryDao.replaceForPlaylist(playlistId, categories)
                vodDao.replaceForPlaylist(playlistId, items)
            }
        }

    /**
     * Fetches and replaces all series categories/list for [playlistId].
     */
    suspend fun syncSeries(playlistId: String, credentials: XtreamCredentials): Result<Unit> =
        withContext(Dispatchers.IO) {
            runSync("Kunde inte synka serieinnehåll") {
                val api = apiFactory.create(credentials.serverUrl)
                val categories = api.getSeriesCategories(
                    username = credentials.username,
                    password = credentials.password
                ).map { it.toSeriesCategoryEntity(playlistId) }
                val seriesRows = api.getSeries(
                    username = credentials.username,
                    password = credentials.password
                ).mapIndexed { index, dto ->
                    dto.toEntity(playlistId, index)
                }
                seriesCategoryDao.replaceForPlaylist(playlistId, categories)
                seriesDao.replaceForPlaylist(playlistId, seriesRows)
            }
        }

    /**
     * Fetches and replaces all episodes for the target series row.
     */
    suspend fun syncSeriesEpisodes(
        seriesRowId: Long,
        seriesId: Int,
        credentials: XtreamCredentials
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runSync("Kunde inte synka serieavsnitt") {
            val api = apiFactory.create(credentials.serverUrl)
            val info = api.getSeriesInfo(
                username = credentials.username,
                password = credentials.password,
                seriesId = seriesId
            )
            val flattenedEpisodes = info.episodes
                ?.values
                ?.flatMap { jsonElement ->
                    jsonElement.toEpisodeDtoList()
                }
                .orEmpty()

            val entities = flattenedEpisodes.mapIndexed { index, episode ->
                episode.toEntity(seriesRowId = seriesRowId, sortOrder = index)
            }
            episodeDao.replaceForSeries(seriesRowId, entities)
        }
    }

    private suspend fun runSync(
        defaultMessage: String,
        block: suspend () -> Unit
    ): Result<Unit> {
        return try {
            block()
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(IOException("$defaultMessage: nätverksfel (${e.message})", e))
        } catch (e: HttpException) {
            Result.failure(IOException("$defaultMessage: serverfel (${e.code()})", e))
        } catch (e: Exception) {
            Result.failure(IOException("$defaultMessage: ${e.message ?: "okänt fel"}", e))
        }
    }
}

private val episodeJson = kotlinx.serialization.json.Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
}

private fun kotlinx.serialization.json.JsonElement.toEpisodeDtoList():
    List<com.valladares.iptvplayer.data.xtream.dto.XtreamEpisodeDto> {
    if (this !is kotlinx.serialization.json.JsonArray) {
        return emptyList()
    }
    return this.mapNotNull { element ->
        runCatching {
            episodeJson.decodeFromJsonElement(
                com.valladares.iptvplayer.data.xtream.dto.XtreamEpisodeDto.serializer(),
                element
            )
        }.getOrNull()
    }
}
