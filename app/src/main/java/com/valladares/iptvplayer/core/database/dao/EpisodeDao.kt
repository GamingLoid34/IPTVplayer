package com.valladares.iptvplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.valladares.iptvplayer.core.database.entity.EpisodeEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access for Xtream episode rows.
 */
@Dao
interface EpisodeDao {
    /**
     * Observes episodes by series row id.
     */
    @Query("SELECT * FROM episodes WHERE seriesRowId = :seriesRowId ORDER BY sortOrder ASC")
    fun observeBySeriesRowId(seriesRowId: Long): Flow<List<EpisodeEntity>>

    /**
     * Returns one episode by row id.
     */
    @Query("SELECT * FROM episodes WHERE id = :id")
    suspend fun getById(id: Long): EpisodeEntity?

    /**
     * Inserts or replaces episode rows.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(episodes: List<EpisodeEntity>)

    /**
     * Deletes all episodes for a series row.
     */
    @Query("DELETE FROM episodes WHERE seriesRowId = :seriesRowId")
    suspend fun deleteBySeriesRowId(seriesRowId: Long)

    /**
     * Replaces all episodes for a series row.
     */
    @Transaction
    suspend fun replaceForSeries(seriesRowId: Long, episodes: List<EpisodeEntity>) {
        deleteBySeriesRowId(seriesRowId)
        insertAll(episodes)
    }
}
