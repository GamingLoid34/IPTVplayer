package com.valladares.iptvplayer.data.xtream

import com.valladares.iptvplayer.core.database.dao.LiveCategoryDao
import com.valladares.iptvplayer.core.database.dao.LiveChannelDao
import com.valladares.iptvplayer.core.database.mapper.toDomain
import com.valladares.iptvplayer.data.xtream.model.LiveChannel
import com.valladares.iptvplayer.data.xtream.model.XtreamCategory
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed [LiveRepository] implementation.
 */
@Singleton
class LiveRepositoryImpl @Inject constructor(
    private val liveChannelDao: LiveChannelDao,
    private val liveCategoryDao: LiveCategoryDao
) : LiveRepository {
    override fun observeCategories(playlistId: String): Flow<List<XtreamCategory>> =
        liveCategoryDao.observeByPlaylistId(playlistId).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeChannels(playlistId: String): Flow<List<LiveChannel>> =
        liveChannelDao.observeByPlaylistId(playlistId).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeChannelsByCategory(
        playlistId: String,
        categoryExternalId: String?
    ): Flow<List<LiveChannel>> =
        liveChannelDao.observeByPlaylistAndCategory(playlistId, categoryExternalId).map { rows ->
            rows.map { it.toDomain() }
        }

    override suspend fun getChannel(id: Long): LiveChannel? =
        liveChannelDao.getById(id)?.toDomain()
}
