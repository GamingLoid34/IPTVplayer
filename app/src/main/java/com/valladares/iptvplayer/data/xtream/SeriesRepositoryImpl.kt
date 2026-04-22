package com.valladares.iptvplayer.data.xtream

import com.valladares.iptvplayer.core.database.dao.EpisodeDao
import com.valladares.iptvplayer.core.database.dao.SeriesCategoryDao
import com.valladares.iptvplayer.core.database.dao.SeriesDao
import com.valladares.iptvplayer.core.database.mapper.toDomain
import com.valladares.iptvplayer.data.xtream.model.Episode
import com.valladares.iptvplayer.data.xtream.model.Series
import com.valladares.iptvplayer.data.xtream.model.XtreamCategory
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed [SeriesRepository] implementation.
 */
@Singleton
class SeriesRepositoryImpl @Inject constructor(
    private val seriesDao: SeriesDao,
    private val seriesCategoryDao: SeriesCategoryDao,
    private val episodeDao: EpisodeDao
) : SeriesRepository {
    override fun observeCategories(playlistId: String): Flow<List<XtreamCategory>> =
        seriesCategoryDao.observeByPlaylistId(playlistId).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeSeriesList(playlistId: String): Flow<List<Series>> =
        seriesDao.observeByPlaylistId(playlistId).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeSeriesByCategory(
        playlistId: String,
        categoryExternalId: String?
    ): Flow<List<Series>> =
        seriesDao.observeByPlaylistAndCategory(playlistId, categoryExternalId).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeEpisodes(seriesRowId: Long): Flow<List<Episode>> =
        episodeDao.observeBySeriesRowId(seriesRowId).map { rows ->
            rows.map { it.toDomain() }
        }

    override suspend fun getSeries(id: Long): Series? =
        seriesDao.getById(id)?.toDomain()

    override suspend fun getEpisode(id: Long): Episode? =
        episodeDao.getById(id)?.toDomain()
}
