package com.example.pulseplayer.data.repository

import com.example.pulseplayer.data.entity.Playlist
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    val allPlaylists: Flow<List<Playlist>>
    suspend fun insert(playlist: Playlist)
    suspend fun update(playlist: Playlist)
    suspend fun delete(playlist: Playlist)
}
