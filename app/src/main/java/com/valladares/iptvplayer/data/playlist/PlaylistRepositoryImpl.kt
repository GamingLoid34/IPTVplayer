package com.valladares.iptvplayer.data.playlist

import com.valladares.iptvplayer.core.database.dao.ChannelDao
import com.valladares.iptvplayer.core.database.dao.PlaylistDao
import com.valladares.iptvplayer.core.database.entity.PlaylistEntity
import com.valladares.iptvplayer.core.database.mapper.toDomain
import com.valladares.iptvplayer.core.database.mapper.toEntity
import com.valladares.iptvplayer.core.common.AppConstants
import com.valladares.iptvplayer.data.playlist.model.Channel
import com.valladares.iptvplayer.data.playlist.model.PlaybackHeaders
import com.valladares.iptvplayer.data.playlist.model.Playlist
import com.valladares.iptvplayer.data.playlist.model.PlaylistSourceType
import com.valladares.iptvplayer.data.playlist.parser.M3UParser
import com.valladares.iptvplayer.data.xtream.XtreamAuthenticator
import com.valladares.iptvplayer.data.xtream.XtreamSyncService
import com.valladares.iptvplayer.data.xtream.model.XtreamAuthFailureReason
import com.valladares.iptvplayer.data.xtream.model.XtreamAuthStatus
import com.valladares.iptvplayer.data.xtream.model.XtreamCredentials
import java.io.IOException
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
    private val m3uParser: M3UParser,
    private val xtreamAuthenticator: XtreamAuthenticator,
    private val xtreamSyncService: XtreamSyncService
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
     * @see PlaylistRepository.importXtreamPlaylist
     */
    override suspend fun importXtreamPlaylist(
        name: String,
        credentials: XtreamCredentials
    ): Result<String> {
        return try {
            when (val authStatus = xtreamAuthenticator.authenticate(credentials)) {
                is XtreamAuthStatus.Failed -> {
                    val errorKey = when (authStatus.reason) {
                        XtreamAuthFailureReason.INVALID_CREDENTIALS ->
                            "import_error_xtream_auth_invalid_credentials"

                        XtreamAuthFailureReason.ACCOUNT_EXPIRED ->
                            "import_error_xtream_auth_expired"

                        XtreamAuthFailureReason.ACCOUNT_DISABLED ->
                            "import_error_xtream_auth_disabled"

                        XtreamAuthFailureReason.NETWORK_ERROR ->
                            "import_error_xtream_network"

                        XtreamAuthFailureReason.SERVER_ERROR ->
                            "import_error_xtream_server_error"

                        XtreamAuthFailureReason.UNKNOWN ->
                            "import_error_xtream_unknown"
                    }
                    Result.failure(IllegalStateException(errorKey))
                }

                is XtreamAuthStatus.Success -> {
                    val playlistId = UUID.randomUUID().toString()
                    val now = System.currentTimeMillis()
                    val entity = PlaylistEntity(
                        id = playlistId,
                        name = name,
                        sourceType = PlaylistSourceType.XTREAM.name,
                        sourceUri = credentials.serverUrl,
                        createdAt = now,
                        updatedAt = now,
                        xtreamServerUrl = credentials.serverUrl,
                        xtreamUsername = credentials.username,
                        xtreamPassword = credentials.password
                    )
                    playlistDao.insert(entity)
                    // Sync error should not block successful Xtream import if credentials are valid.
                    xtreamSyncService.syncLive(playlistId, credentials)
                    Result.success(playlistId)
                }
            }
        } catch (e: IOException) {
            Result.failure(IOException("import_error_xtream_network", e))
        } catch (e: Exception) {
            Result.failure(IllegalStateException("import_error_xtream_unknown", e))
        }
    }

    /**
     * @see PlaylistRepository.deletePlaylist
     */
    override suspend fun deletePlaylist(playlistId: String) {
        val existing = playlistDao.getById(playlistId) ?: return
        playlistDao.delete(existing)
    }

    /**
     * @see PlaylistRepository.getXtreamCredentials
     */
    override suspend fun getXtreamCredentials(playlistId: String): XtreamCredentials? {
        val entity = playlistDao.getById(playlistId) ?: return null
        val server = entity.xtreamServerUrl
        val user = entity.xtreamUsername
        val pass = entity.xtreamPassword
        return if (server != null && user != null && pass != null) {
            XtreamCredentials(
                serverUrl = server,
                username = user,
                password = pass
            )
        } else {
            null
        }
    }

    /**
     * @see PlaylistRepository.getPlaybackHeaders
     */
    override suspend fun getPlaybackHeaders(playlistId: String): PlaybackHeaders {
        val playlist = playlistDao.getById(playlistId)
        return PlaybackHeaders(
            userAgent = playlist?.userAgent ?: AppConstants.DEFAULT_USER_AGENT,
            referer = playlist?.referer
        )
    }

    /**
     * @see PlaylistRepository.updatePlaybackHeaders
     */
    override suspend fun updatePlaybackHeaders(
        playlistId: String,
        userAgent: String?,
        referer: String?
    ) {
        val normalizedUserAgent = userAgent?.trim()?.takeIf { it.isNotEmpty() }
        val normalizedReferer = referer?.trim()?.takeIf { it.isNotEmpty() }
        playlistDao.updatePlaybackHeaders(
            playlistId = playlistId,
            userAgent = normalizedUserAgent,
            referer = normalizedReferer
        )
    }
}
