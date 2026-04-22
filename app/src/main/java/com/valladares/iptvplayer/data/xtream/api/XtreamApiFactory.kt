package com.valladares.iptvplayer.data.xtream.api

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Creates [XtreamApi] instances for dynamic server URLs.
 *
 * Xtream providers vary per user/server, so base URL cannot be static in DI.
 */
@Singleton
class XtreamApiFactory @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json
) {
    /**
     * Builds a Retrofit-backed [XtreamApi] for [serverUrl].
     */
    fun create(serverUrl: String): XtreamApi {
        val normalizedUrl = serverUrl.trim().trimEnd('/') + "/"
        val retrofit = Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        return retrofit.create(XtreamApi::class.java)
    }
}
