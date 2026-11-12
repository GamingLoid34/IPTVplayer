# IPTVplayer

Personal Android IPTV player built with Jetpack Compose, Media3 ExoPlayer, and Xtream Codes API.

## Features
- Xtream Codes integration (auth, sync, credentials per playlist)
- Live TV, VOD, and Series tabs
- Search, country filter, favorites, watch history
- Per-playlist User-Agent and Referer configuration
- Fullscreen with auto-landscape
- Keep-screen-on during playback

## Known limitations
- VobSub/PGS subtitles filtered (Media3 does not support bitmap subtitle rendering)
- Cast removed (default Chromecast receiver cannot set HTTP headers)

## Stack
Kotlin 2.0, Jetpack Compose, Hilt, Room, Media3 1.5.1, Retrofit, kotlinx-serialization