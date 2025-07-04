package com.example.pulseplayer.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.pulseplayer.data.entity.PlaylistSong
import com.example.pulseplayer.data.entity.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistSongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlistSong: PlaylistSong)

    @Update
    suspend fun update(playlistSong: PlaylistSong)

    @Delete
    suspend fun delete(playlistSong: PlaylistSong)

    @Query("""
    SELECT song.* FROM song 
    INNER JOIN playlist_song ON song.id_song = playlist_song.id_song
    WHERE playlist_song.playlist_id = :playlistId
    ORDER BY playlist_song.song_order ASC
""")
    fun getSongsInPlaylist(playlistId: Int): Flow<List<Song>>



}
