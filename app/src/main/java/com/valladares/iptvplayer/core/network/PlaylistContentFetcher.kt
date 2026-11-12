package com.valladares.iptvplayer.core.network

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Fetches raw playlist content from remote URLs or local SAF URIs.
 */
interface PlaylistContentFetcher {
    /**
     * Downloads playlist content from [url].
     */
    suspend fun fetchFromUrl(url: String): Result<String>

    /**
     * Reads playlist content from a content [uri] string.
     */
    suspend fun fetchFromUri(uri: String): Result<String>
}

/**
 * Default [PlaylistContentFetcher] implementation backed by OkHttp and ContentResolver.
 */
class PlaylistContentFetcherImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) : PlaylistContentFetcher {
    override suspend fun fetchFromUrl(url: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Kunde inte hämta URL (HTTP ${response.code})")
                }
                response.body?.string()
                    ?: throw IOException("Tomt svar från URL")
            }
        }.mapFailureMessage("Kunde inte läsa spellistan från URL")
    }

    override suspend fun fetchFromUri(uri: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val parsedUri = Uri.parse(uri)
            context.contentResolver.openInputStream(parsedUri)?.bufferedReader()?.use {
                it.readText()
            } ?: throw IOException("Kunde inte öppna vald fil")
        }.mapFailureMessage("Kunde inte läsa spellistan från fil")
    }
}

private fun Result<String>.mapFailureMessage(prefix: String): Result<String> {
    val error = exceptionOrNull() ?: return this
    val message = error.message?.takeIf { it.isNotBlank() } ?: "okänt fel"
    return Result.failure(IOException("$prefix: $message", error))
}
