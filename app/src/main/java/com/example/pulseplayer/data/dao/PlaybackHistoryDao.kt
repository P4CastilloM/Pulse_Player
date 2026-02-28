package com.example.pulseplayer.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pulseplayer.data.entity.PlaybackHistory
import com.example.pulseplayer.data.entity.SmartPlaylistTrack
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

    @Query("DELETE FROM playback_history")
    suspend fun deleteAll()

    @Query("DELETE FROM playback_history WHERE id_song = :songId")
    suspend fun deleteBySongId(songId: Int)


    @Query("""
        SELECT s.id_song, s.title, s.artist_name, s.cover_image, s.file_path, s.duration_ms, COUNT(h.id) AS play_count
        FROM playback_history h
        INNER JOIN song s ON s.id_song = h.id_song
        WHERE h.played_at >= :fromDate
        GROUP BY s.id_song
        ORDER BY play_count DESC, MAX(h.played_at) DESC
        LIMIT :limit
    """)
    suspend fun getMostPlayedSince(fromDate: String, limit: Int = 50): List<SmartPlaylistTrack>

    @Query("""
        SELECT s.id_song, s.title, s.artist_name, s.cover_image, s.file_path, s.duration_ms, COUNT(h.id) AS play_count
        FROM playback_history h
        INNER JOIN song s ON s.id_song = h.id_song
        WHERE h.id_song IN (
            SELECT id_song
            FROM playback_history
            GROUP BY id_song
            HAVING MIN(played_at) >= :fromDate
        )
        GROUP BY s.id_song
        ORDER BY MIN(h.played_at) DESC
        LIMIT :limit
    """)
    suspend fun getRecentlyDiscovered(fromDate: String, limit: Int = 50): List<SmartPlaylistTrack>

    @Query("""
        SELECT s.id_song, s.title, s.artist_name, s.cover_image, s.file_path, s.duration_ms, COALESCE(COUNT(h.id), 0) AS play_count
        FROM song s
        LEFT JOIN playback_history h ON h.id_song = s.id_song
        GROUP BY s.id_song
        HAVING MAX(h.played_at) IS NULL OR MAX(h.played_at) < :cutoffDate
        ORDER BY COALESCE(MAX(h.played_at), '1900-01-01 00:00:00') ASC, s.title ASC
        LIMIT :limit
    """)
    suspend fun getNotPlayedSince(cutoffDate: String, limit: Int = 50): List<SmartPlaylistTrack>

    @Query("""
        SELECT s.id_song, s.title, s.artist_name, s.cover_image, s.file_path, s.duration_ms, COUNT(h.id) AS play_count
        FROM playback_history h
        INNER JOIN song s ON s.id_song = h.id_song
        WHERE CAST(strftime('%H', h.played_at) AS INTEGER) >= 22
           OR CAST(strftime('%H', h.played_at) AS INTEGER) < 6
        GROUP BY s.id_song
        ORDER BY play_count DESC, s.title ASC
        LIMIT :limit
    """)
    suspend fun getTopNightTracks(limit: Int = 50): List<SmartPlaylistTrack>

    @Query("""
        SELECT s.id_song, s.title, s.artist_name, s.cover_image, s.file_path, s.duration_ms, COALESCE(COUNT(h.id), 0) AS play_count
        FROM song s
        LEFT JOIN playback_history h ON h.id_song = s.id_song
        WHERE LOWER(s.title) NOT LIKE '%remix%'
          AND LOWER(COALESCE(s.genre, '')) NOT LIKE '%edm%'
          AND LOWER(COALESCE(s.genre, '')) NOT LIKE '%dance%'
          AND LOWER(COALESCE(s.genre, '')) NOT LIKE '%house%'
        GROUP BY s.id_song
        ORDER BY play_count ASC, s.duration_ms DESC, s.title ASC
        LIMIT :limit
    """)
    suspend fun getStudyModeTracks(limit: Int = 50): List<SmartPlaylistTrack>



    @Query("SELECT COUNT(*) FROM playback_history WHERE played_at LIKE :dayPrefix || '%'")
    suspend fun getCountForDay(dayPrefix: String): Int

    @Query("SELECT COUNT(DISTINCT id_song) FROM playback_history")
    suspend fun getUniqueTracksCount(): Int

    @Query("""
        SELECT COALESCE(SUM(COALESCE(h.played_ms, s.duration_ms)), 0)
        FROM playback_history h
        INNER JOIN song s ON s.id_song = h.id_song
    """)
    suspend fun getTotalListeningMs(): Long

    @Query("""
        SELECT COALESCE(SUM(COALESCE(h.played_ms, s.duration_ms)), 0)
        FROM playback_history h
        INNER JOIN song s ON s.id_song = h.id_song
        WHERE h.played_at LIKE :dayPrefix || '%'
    """)
    suspend fun getListeningMsForDay(dayPrefix: String): Long

}