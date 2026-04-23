package com.valladares.iptvplayer.data.xtream.api

import com.valladares.iptvplayer.core.network.StreamingJsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import retrofit2.Retrofit

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
            .addConverterFactory(StreamingJsonConverterFactory(json))
            .build()
        return retrofit.create(XtreamApi::class.java)
    }
}
