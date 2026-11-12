package com.valladares.iptvplayer.core.common

/**
 * App-wide constants used by the MVP foundation.
 */
object AppConstants {
    /**
     * User-Agent som efterliknar VLC. Många IPTV-servrar (särskilt Xtream Codes) blockerar default
     * Android/OkHttp User-Agent med HTTP 458. VLC:s User-Agent är brett accepterad.
     */
    const val DEFAULT_USER_AGENT: String = "Lavf/60.3.100"

    /**
     * Public HLS stream used as a safe MVP playback test source.
     */
    const val DEFAULT_TEST_STREAM_URL: String =
        "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
}
