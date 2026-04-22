package com.valladares.iptvplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.valladares.iptvplayer.core.database.entity.PlaylistEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access for [PlaylistEntity] rows.
 */
@Dao
interface PlaylistDao {
    /**
     * Inserts or replaces a playlist by primary key.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlist: PlaylistEntity)

    /**
     * Updates an existing playlist row.
     */
    @Update
    suspend fun update(playlist: PlaylistEntity)

    /**
     * Deletes a playlist row (channels cascade if configured on the entity).
     */
    @Delete
    suspend fun delete(playlist: PlaylistEntity)

    /**
     * Observes all playlists, newest [PlaylistEntity.updatedAt] first.
     */
    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<PlaylistEntity>>

    /**
     * Returns a single playlist by id, or null if missing.
     */
    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getById(id: String): PlaylistEntity?
}
