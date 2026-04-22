package com.valladares.iptvplayer.di

import com.valladares.iptvplayer.BuildConfig
import com.valladares.iptvplayer.core.network.PlaylistContentFetcher
import com.valladares.iptvplayer.core.network.PlaylistContentFetcherImpl
import com.valladares.iptvplayer.data.xtream.model.XtreamStreamUrls
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Provides network dependencies used by playlist import.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    /**
     * Builds the app-wide OkHttp client with conservative import timeouts.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            builder.addInterceptor(logging)
        }
        return builder.build()
    }

    /**
     * Provides a lenient JSON parser for inconsistent Xtream payloads.
     */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    /**
     * Injects the stateless [XtreamStreamUrls] factory (for Hilt view models and repositories).
     */
    @Provides
    @Singleton
    fun provideXtreamStreamUrls(): XtreamStreamUrls = XtreamStreamUrls
}

/**
 * Binds network-related interfaces to concrete implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindModule {
    /**
     * Binds [PlaylistContentFetcher] to [PlaylistContentFetcherImpl].
     */
    @Binds
    @Singleton
    abstract fun bindPlaylistContentFetcher(
        impl: PlaylistContentFetcherImpl
    ): PlaylistContentFetcher
}
