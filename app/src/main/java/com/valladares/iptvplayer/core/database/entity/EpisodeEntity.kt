package com.valladares.iptvplayer.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room row for Xtream episodes associated with a series row.
 */
@Entity(
    tableName = "episodes",
    foreignKeys = [
        ForeignKey(
            entity = SeriesEntity::class,
            parentColumns = ["id"],
            childColumns = ["seriesRowId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("seriesRowId")]
)
data class EpisodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val seriesRowId: Long,
    val externalId: String,
    val title: String,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val containerExtension: String?,
    val plot: String?,
    val durationSecs: Int?,
    val movieImage: String?,
    val sortOrder: Int
)
