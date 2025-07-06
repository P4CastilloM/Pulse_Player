package com.example.pulseplayer.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pulseplayer.data.entity.PlaybackHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaybackHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: PlaybackHistory)

    @Update
    suspend fun update(history: PlaybackHistory)

    @Delete
    suspend fun delete(history: PlaybackHistory)

    @Query("SELECT * FROM playback_history ORDER BY played_at DESC")
    fun getAll(): Flow<List<PlaybackHistory>>

    @Query("SELECT COUNT(*) FROM playback_history")
    suspend fun getCount(): Int

    @Query("DELETE FROM playback_history WHERE id IN (SELECT id FROM playback_history ORDER BY played_at ASC LIMIT :limit)")
    suspend fun deleteOldest(limit: Int)

    @Query("SELECT * FROM playback_history ORDER BY played_at DESC")
    suspend fun getAllOnce(): List<PlaybackHistory> // 🔁 Para cargar una vez en pantalla


}