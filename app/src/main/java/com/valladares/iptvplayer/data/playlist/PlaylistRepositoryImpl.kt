package com.valladares.iptvplayer.data.playlist

import com.valladares.iptvplayer.core.database.dao.ChannelDao
import com.valladares.iptvplayer.core.database.dao.PlaylistDao
import com.valladares.iptvplayer.core.database.entity.PlaylistEntity
import com.valladares.iptvplayer.core.database.mapper.toDomain
import com.valladares.iptvplayer.core.database.mapper.toEntity
import com.valladares.iptvplayer.data.playlist.model.Channel
import com.valladares.iptvplayer.data.playlist.model.Playlist
import com.valladares.iptvplayer.data.playlist.model.PlaylistSourceType
import com.valladares.iptvplayer.data.playlist.parser.M3UParser
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * [PlaylistRepository] implementation backed by Room and [M3UParser].
 */
@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val channelDao: ChannelDao,
    private val m3uParser: M3UParser
) : PlaylistRepository {

    /**
     * @see PlaylistRepository.observePlaylists
     */
    override fun observePlaylists(): Flow<List<Playlist>> {
        return playlistDao.observeAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    /**
     * @see PlaylistRepository.observeChannels
     */
    override fun observeChannels(playlistId: String): Flow<List<Channel>> {
        return channelDao.observeByPlaylistId(playlistId).map { list ->
            list.map { it.toDomain() }
        }
    }

    /**
     * @see PlaylistRepository.importPlaylistFromContent
     */
    override suspend fun importPlaylistFromContent(
        name: String,
        sourceType: PlaylistSourceType,
        sourceUri: String,
        content: String
    ): Result<String> {
        val parsed = m3uParser.parse(content)
        if (parsed.isEmpty()) {
            return Result.failure(
                IllegalArgumentException("Spellistan är tom eller ogiltig")
            )
        }
        val playlistId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val entity = PlaylistEntity(
            id = playlistId,
            name = name,
            sourceType = sourceType.name,
            sourceUri = sourceUri,
            createdAt = now,
            updatedAt = now
        )
        playlistDao.insert(entity)
        val channelEntities = parsed.mapIndexed { index, channel ->
            channel.toEntity(playlistId, index)
        }
        channelDao.replaceChannelsForPlaylist(playlistId, channelEntities)
        return Result.success(playlistId)
    }

    /**
     * @see PlaylistRepository.deletePlaylist
     */
    override suspend fun deletePlaylist(playlistId: String) {
        val existing = playlistDao.getById(playlistId) ?: return
        playlistDao.delete(existing)
    }
}
