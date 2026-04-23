package com.valladares.iptvplayer.di

import android.content.Context
import androidx.room.Room
import com.valladares.iptvplayer.core.database.IPTVDatabase
import com.valladares.iptvplayer.core.database.dao.ChannelDao
import com.valladares.iptvplayer.core.database.dao.EpisodeDao
import com.valladares.iptvplayer.core.database.dao.FavoriteChannelDao
import com.valladares.iptvplayer.core.database.dao.LiveCategoryDao
import com.valladares.iptvplayer.core.database.dao.LiveChannelDao
import com.valladares.iptvplayer.core.database.dao.PlaylistDao
import com.valladares.iptvplayer.core.database.dao.SeriesCategoryDao
import com.valladares.iptvplayer.core.database.dao.SeriesDao
import com.valladares.iptvplayer.core.database.dao.VodCategoryDao
import com.valladares.iptvplayer.core.database.dao.VodDao
import com.valladares.iptvplayer.core.database.dao.WatchHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides the Room [IPTVDatabase] and DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    /**
     * Provides the singleton [IPTVDatabase] instance.
     *
     * Uses destructive migration on version upgrade so MVP does not require a
     * hand-written Room migration; existing local data is cleared when the DB version changes.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): IPTVDatabase {
        return Room.databaseBuilder(
            context,
            IPTVDatabase::class.java,
            "iptvplayer.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Provides the playlist [PlaylistDao].
     */
    @Provides
    fun providePlaylistDao(db: IPTVDatabase): PlaylistDao = db.playlistDao()

    /**
     * Provides the channel [ChannelDao].
     */
    @Provides
    fun provideChannelDao(db: IPTVDatabase): ChannelDao = db.channelDao()

    /**
     * Provides the live category [LiveCategoryDao].
     */
    @Provides
    fun provideLiveCategoryDao(db: IPTVDatabase): LiveCategoryDao = db.liveCategoryDao()

    /**
     * Provides the live channel [LiveChannelDao].
     */
    @Provides
    fun provideLiveChannelDao(db: IPTVDatabase): LiveChannelDao = db.liveChannelDao()

    /**
     * Provides the VOD category [VodCategoryDao].
     */
    @Provides
    fun provideVodCategoryDao(db: IPTVDatabase): VodCategoryDao = db.vodCategoryDao()

    /**
     * Provides the VOD [VodDao].
     */
    @Provides
    fun provideVodDao(db: IPTVDatabase): VodDao = db.vodDao()

    /**
     * Provides the series category [SeriesCategoryDao].
     */
    @Provides
    fun provideSeriesCategoryDao(db: IPTVDatabase): SeriesCategoryDao = db.seriesCategoryDao()

    /**
     * Provides the series [SeriesDao].
     */
    @Provides
    fun provideSeriesDao(db: IPTVDatabase): SeriesDao = db.seriesDao()

    /**
     * Provides the episode [EpisodeDao].
     */
    @Provides
    fun provideEpisodeDao(db: IPTVDatabase): EpisodeDao = db.episodeDao()

    /**
     * Provides the favorites [FavoriteChannelDao].
     */
    @Provides
    fun provideFavoriteChannelDao(db: IPTVDatabase): FavoriteChannelDao = db.favoriteChannelDao()

    /**
     * Provides the history [WatchHistoryDao].
     */
    @Provides
    fun provideWatchHistoryDao(db: IPTVDatabase): WatchHistoryDao = db.watchHistoryDao()
}
