package com.example.pulseplayer.data.repository

import com.example.pulseplayer.data.entity.PlaylistSong
import com.example.pulseplayer.data.entity.Song
import kotlinx.coroutines.flow.Flow

interface PlaylistSongRepository {
    fun getSongsInPlaylist(playlistId: Int): Flow<List<Song>>
    suspend fun insert(playlistSong: PlaylistSong)
    suspend fun update(playlistSong: PlaylistSong)
    suspend fun delete(playlistSong: PlaylistSong)
}
