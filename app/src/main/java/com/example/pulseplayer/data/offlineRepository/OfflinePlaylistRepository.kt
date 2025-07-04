package com.example.pulseplayer.data.offlineRepository

import com.example.pulseplayer.data.dao.PlaylistDao
import com.example.pulseplayer.data.entity.Playlist
import com.example.pulseplayer.data.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow

class OfflinePlaylistRepository(private val dao: PlaylistDao) : PlaylistRepository {
    override val allPlaylists: Flow<List<Playlist>> = dao.getAll()

    override suspend fun insert(playlist: Playlist) = dao.insert(playlist)
    override suspend fun update(playlist: Playlist) = dao.update(playlist)
    override suspend fun delete(playlist: Playlist) = dao.delete(playlist)
}