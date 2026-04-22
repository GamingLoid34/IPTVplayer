package com.valladares.iptvplayer.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.valladares.iptvplayer.core.database.dao.ChannelDao
import com.valladares.iptvplayer.core.database.dao.EpisodeDao
import com.valladares.iptvplayer.core.database.dao.LiveCategoryDao
import com.valladares.iptvplayer.core.database.dao.LiveChannelDao
import com.valladares.iptvplayer.core.database.dao.PlaylistDao
import com.valladares.iptvplayer.core.database.dao.SeriesCategoryDao
import com.valladares.iptvplayer.core.database.dao.SeriesDao
import com.valladares.iptvplayer.core.database.dao.VodCategoryDao
import com.valladares.iptvplayer.core.database.dao.VodDao
import com.valladares.iptvplayer.core.database.entity.ChannelEntity
import com.valladares.iptvplayer.core.database.entity.EpisodeEntity
import com.valladares.iptvplayer.core.database.entity.LiveCategoryEntity
import com.valladares.iptvplayer.core.database.entity.LiveChannelEntity
import com.valladares.iptvplayer.core.database.entity.PlaylistEntity
import com.valladares.iptvplayer.core.database.entity.SeriesCategoryEntity
import com.valladares.iptvplayer.core.database.entity.SeriesEntity
import com.valladares.iptvplayer.core.database.entity.VodCategoryEntity
import com.valladares.iptvplayer.core.database.entity.VodEntity

/**
 * Application Room database for playlists and channel rows.
 */
@Database(
    entities = [
        PlaylistEntity::class,
        ChannelEntity::class,
        LiveChannelEntity::class,
        LiveCategoryEntity::class,
        VodEntity::class,
        VodCategoryEntity::class,
        SeriesEntity::class,
        SeriesCategoryEntity::class,
        EpisodeEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class IPTVDatabase : RoomDatabase() {
    /**
     * Provides access to playlist persistence.
     */
    abstract fun playlistDao(): PlaylistDao

    /**
     * Provides access to channel persistence.
     */
    abstract fun channelDao(): ChannelDao

    /**
     * Provides access to live category persistence.
     */
    abstract fun liveCategoryDao(): LiveCategoryDao

    /**
     * Provides access to live channel persistence.
     */
    abstract fun liveChannelDao(): LiveChannelDao

    /**
     * Provides access to VOD category persistence.
     */
    abstract fun vodCategoryDao(): VodCategoryDao

    /**
     * Provides access to VOD item persistence.
     */
    abstract fun vodDao(): VodDao

    /**
     * Provides access to series category persistence.
     */
    abstract fun seriesCategoryDao(): SeriesCategoryDao

    /**
     * Provides access to series persistence.
     */
    abstract fun seriesDao(): SeriesDao

    /**
     * Provides access to episode persistence.
     */
    abstract fun episodeDao(): EpisodeDao
}
