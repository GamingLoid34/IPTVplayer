package com.valladares.iptvplayer.data.xtream.api

import com.valladares.iptvplayer.data.xtream.dto.XtreamAuthResponseDto
import com.valladares.iptvplayer.data.xtream.dto.XtreamCategoryDto
import com.valladares.iptvplayer.data.xtream.dto.XtreamLiveStreamDto
import com.valladares.iptvplayer.data.xtream.dto.XtreamSeriesDto
import com.valladares.iptvplayer.data.xtream.dto.XtreamSeriesInfoDto
import com.valladares.iptvplayer.data.xtream.dto.XtreamVodInfoDto
import com.valladares.iptvplayer.data.xtream.dto.XtreamVodStreamDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit contract for Xtream Codes `player_api.php` endpoints.
 */
interface XtreamApi {
    /**
     * Validates user credentials.
     */
    @GET("player_api.php")
    suspend fun authenticate(
        @Query("username") username: String,
        @Query("password") password: String
    ): XtreamAuthResponseDto

    /**
     * Returns live categories.
     */
    @GET("player_api.php")
    suspend fun getLiveCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_categories"
    ): List<XtreamCategoryDto>

    /**
     * Returns live streams.
     */
    @GET("player_api.php")
    suspend fun getLiveStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_streams"
    ): List<XtreamLiveStreamDto>

    /**
     * Returns VOD categories.
     */
    @GET("player_api.php")
    suspend fun getVodCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_categories"
    ): List<XtreamCategoryDto>

    /**
     * Returns VOD streams.
     */
    @GET("player_api.php")
    suspend fun getVodStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_streams"
    ): List<XtreamVodStreamDto>

    /**
     * Returns VOD detail (including subtitles) for a specific [vodId].
     */
    @GET("player_api.php")
    suspend fun getVodInfo(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("vod_id") vodId: Int,
        @Query("action") action: String = "get_vod_info"
    ): XtreamVodInfoDto

    /**
     * Returns series categories.
     */
    @GET("player_api.php")
    suspend fun getSeriesCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series_categories"
    ): List<XtreamCategoryDto>

    /**
     * Returns series list.
     */
    @GET("player_api.php")
    suspend fun getSeries(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series"
    ): List<XtreamSeriesDto>

    /**
     * Returns full series detail for a specific [seriesId].
     */
    @GET("player_api.php")
    suspend fun getSeriesInfo(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("series_id") seriesId: Int,
        @Query("action") action: String = "get_series_info"
    ): XtreamSeriesInfoDto
}
