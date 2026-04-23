package com.valladares.iptvplayer.core.database.mapper

import com.valladares.iptvplayer.core.database.entity.EpisodeEntity
import com.valladares.iptvplayer.core.database.entity.LiveCategoryEntity
import com.valladares.iptvplayer.core.database.entity.LiveChannelEntity
import com.valladares.iptvplayer.core.database.entity.SeriesCategoryEntity
import com.valladares.iptvplayer.core.database.entity.SeriesEntity
import com.valladares.iptvplayer.core.database.entity.VodCategoryEntity
import com.valladares.iptvplayer.core.database.entity.VodEntity
import com.valladares.iptvplayer.data.xtream.dto.XtreamCategoryDto
import com.valladares.iptvplayer.data.xtream.dto.XtreamEpisodeDto
import com.valladares.iptvplayer.data.xtream.dto.XtreamLiveStreamDto
import com.valladares.iptvplayer.data.xtream.dto.XtreamSeriesDto
import com.valladares.iptvplayer.data.xtream.dto.XtreamVodStreamDto
import com.valladares.iptvplayer.data.xtream.CountryCodeDetector
import com.valladares.iptvplayer.data.xtream.model.Episode
import com.valladares.iptvplayer.data.xtream.model.LiveChannel
import com.valladares.iptvplayer.data.xtream.model.Series
import com.valladares.iptvplayer.data.xtream.model.VodItem
import com.valladares.iptvplayer.data.xtream.model.XtreamCategory

/**
 * Mapper functions between Xtream DTOs, entities and domain models.
 */
fun XtreamCategoryDto.toLiveCategoryEntity(playlistId: String): LiveCategoryEntity = LiveCategoryEntity(
    playlistId = playlistId,
    externalId = categoryId,
    name = categoryName,
    parentId = parentId
)

/**
 * Maps category DTO to VOD category entity.
 */
fun XtreamCategoryDto.toVodCategoryEntity(playlistId: String): VodCategoryEntity = VodCategoryEntity(
    playlistId = playlistId,
    externalId = categoryId,
    name = categoryName,
    parentId = parentId
)

/**
 * Maps category DTO to series category entity.
 */
fun XtreamCategoryDto.toSeriesCategoryEntity(playlistId: String): SeriesCategoryEntity = SeriesCategoryEntity(
    playlistId = playlistId,
    externalId = categoryId,
    name = categoryName,
    parentId = parentId
)

/**
 * Maps Xtream live stream DTO to entity.
 */
fun XtreamLiveStreamDto.toEntity(
    playlistId: String,
    sortOrder: Int,
    categoryName: String?
): LiveChannelEntity = LiveChannelEntity(
    playlistId = playlistId,
    streamId = streamId,
    name = name,
    streamIcon = streamIcon,
    categoryExternalId = categoryId,
    detectedCountryCode = CountryCodeDetector.detect(
        channelName = name,
        categoryName = categoryName
    ),
    epgChannelId = epgChannelId,
    tvArchive = tvArchive,
    tvArchiveDuration = tvArchiveDuration,
    sortOrder = sortOrder
)

/**
 * Maps Xtream VOD stream DTO to entity.
 */
fun XtreamVodStreamDto.toEntity(playlistId: String, sortOrder: Int): VodEntity = VodEntity(
    playlistId = playlistId,
    streamId = streamId,
    name = name,
    streamIcon = streamIcon,
    categoryExternalId = categoryId,
    containerExtension = containerExtension,
    rating = rating,
    rating5Based = rating5Based,
    added = added,
    sortOrder = sortOrder
)

/**
 * Maps Xtream series DTO to entity.
 */
fun XtreamSeriesDto.toEntity(playlistId: String, sortOrder: Int): SeriesEntity = SeriesEntity(
    playlistId = playlistId,
    seriesId = seriesId,
    name = name,
    cover = cover,
    plot = plot,
    cast = cast,
    director = director,
    genre = genre,
    releaseDate = releaseDate,
    rating = rating,
    rating5Based = rating5Based,
    categoryExternalId = categoryId,
    sortOrder = sortOrder
)

/**
 * Maps Xtream episode DTO to entity.
 */
fun XtreamEpisodeDto.toEntity(seriesRowId: Long, sortOrder: Int): EpisodeEntity = EpisodeEntity(
    seriesRowId = seriesRowId,
    externalId = id,
    title = title,
    seasonNumber = season,
    episodeNumber = episodeNum,
    containerExtension = containerExtension,
    plot = info?.plot,
    durationSecs = info?.durationSecs,
    movieImage = info?.movieImage,
    sortOrder = sortOrder
)

/**
 * Maps live category entity to domain model.
 */
fun LiveCategoryEntity.toDomain(): XtreamCategory = XtreamCategory(
    id = id,
    externalId = externalId,
    name = name,
    parentId = parentId
)

/**
 * Maps VOD category entity to domain model.
 */
fun VodCategoryEntity.toDomain(): XtreamCategory = XtreamCategory(
    id = id,
    externalId = externalId,
    name = name,
    parentId = parentId
)

/**
 * Maps series category entity to domain model.
 */
fun SeriesCategoryEntity.toDomain(): XtreamCategory = XtreamCategory(
    id = id,
    externalId = externalId,
    name = name,
    parentId = parentId
)

/**
 * Maps live channel entity to domain model.
 */
fun LiveChannelEntity.toDomain(): LiveChannel = LiveChannel(
    id = id,
    playlistId = playlistId,
    streamId = streamId,
    name = name,
    streamIcon = streamIcon,
    categoryExternalId = categoryExternalId,
    detectedCountryCode = detectedCountryCode,
    epgChannelId = epgChannelId,
    hasTvArchive = tvArchive == 1,
    tvArchiveDuration = tvArchiveDuration
)

/**
 * Maps VOD entity to domain model.
 */
fun VodEntity.toDomain(): VodItem = VodItem(
    id = id,
    playlistId = playlistId,
    streamId = streamId,
    name = name,
    streamIcon = streamIcon,
    categoryExternalId = categoryExternalId,
    containerExtension = containerExtension,
    rating = rating,
    rating5Based = rating5Based,
    added = added
)

/**
 * Maps series entity to domain model.
 */
fun SeriesEntity.toDomain(): Series = Series(
    id = id,
    playlistId = playlistId,
    seriesId = seriesId,
    name = name,
    cover = cover,
    plot = plot,
    cast = cast,
    director = director,
    genre = genre,
    releaseDate = releaseDate,
    rating = rating,
    rating5Based = rating5Based,
    categoryExternalId = categoryExternalId
)

/**
 * Maps episode entity to domain model.
 */
fun EpisodeEntity.toDomain(): Episode = Episode(
    id = id,
    seriesRowId = seriesRowId,
    externalId = externalId,
    title = title,
    seasonNumber = seasonNumber,
    episodeNumber = episodeNumber,
    containerExtension = containerExtension,
    plot = plot,
    durationSecs = durationSecs,
    movieImage = movieImage
)
