package com.valladares.iptvplayer.data.xtream

import com.valladares.iptvplayer.core.database.dao.VodCategoryDao
import com.valladares.iptvplayer.core.database.dao.VodDao
import com.valladares.iptvplayer.core.database.mapper.toDomain
import com.valladares.iptvplayer.data.xtream.model.VodItem
import com.valladares.iptvplayer.data.xtream.model.XtreamCategory
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed [VodRepository] implementation.
 */
@Singleton
class VodRepositoryImpl @Inject constructor(
    private val vodDao: VodDao,
    private val vodCategoryDao: VodCategoryDao
) : VodRepository {
    override fun observeCategories(playlistId: String): Flow<List<XtreamCategory>> =
        vodCategoryDao.observeByPlaylistId(playlistId).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeItems(playlistId: String): Flow<List<VodItem>> =
        vodDao.observeByPlaylistId(playlistId).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeItemsByCategory(
        playlistId: String,
        categoryExternalId: String?
    ): Flow<List<VodItem>> =
        vodDao.observeByPlaylistAndCategory(playlistId, categoryExternalId).map { rows ->
            rows.map { it.toDomain() }
        }

    override suspend fun getItem(id: Long): VodItem? =
        vodDao.getById(id)?.toDomain()
}
