package com.valladares.iptvplayer.di

import com.valladares.iptvplayer.data.playlist.PlaylistRepository
import com.valladares.iptvplayer.data.playlist.PlaylistRepositoryImpl
import com.valladares.iptvplayer.data.xtream.LiveRepository
import com.valladares.iptvplayer.data.xtream.LiveRepositoryImpl
import com.valladares.iptvplayer.data.xtream.SeriesRepository
import com.valladares.iptvplayer.data.xtream.SeriesRepositoryImpl
import com.valladares.iptvplayer.data.xtream.VodRepository
import com.valladares.iptvplayer.data.xtream.VodRepositoryImpl
import com.valladares.iptvplayer.data.xtream.FavoritesRepository
import com.valladares.iptvplayer.data.xtream.FavoritesRepositoryImpl
import com.valladares.iptvplayer.data.xtream.WatchHistoryRepository
import com.valladares.iptvplayer.data.xtream.WatchHistoryRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds repository interfaces to implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    /**
     * Binds [PlaylistRepository] to [PlaylistRepositoryImpl].
     */
    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(impl: PlaylistRepositoryImpl): PlaylistRepository

    /**
     * Binds [LiveRepository] to [LiveRepositoryImpl].
     */
    @Binds
    @Singleton
    abstract fun bindLiveRepository(impl: LiveRepositoryImpl): LiveRepository

    /**
     * Binds [VodRepository] to [VodRepositoryImpl].
     */
    @Binds
    @Singleton
    abstract fun bindVodRepository(impl: VodRepositoryImpl): VodRepository

    /**
     * Binds [SeriesRepository] to [SeriesRepositoryImpl].
     */
    @Binds
    @Singleton
    abstract fun bindSeriesRepository(impl: SeriesRepositoryImpl): SeriesRepository

    /**
     * Binds [FavoritesRepository] to [FavoritesRepositoryImpl].
     */
    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

    /**
     * Binds [WatchHistoryRepository] to [WatchHistoryRepositoryImpl].
     */
    @Binds
    @Singleton
    abstract fun bindWatchHistoryRepository(impl: WatchHistoryRepositoryImpl): WatchHistoryRepository
}
