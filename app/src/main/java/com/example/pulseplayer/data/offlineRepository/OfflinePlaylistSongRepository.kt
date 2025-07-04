package com.example.pulseplayer.data.offlineRepository

import com.example.pulseplayer.data.dao.PlaylistSongDao
import com.example.pulseplayer.data.entity.PlaylistSong
import com.example.pulseplayer.data.entity.Song
import com.example.pulseplayer.data.repository.PlaylistSongRepository
import kotlinx.coroutines.flow.Flow

class OfflinePlaylistSongRepository(private val dao: PlaylistSongDao) : PlaylistSongRepository {
    override fun getSongsInPlaylist(playlistId: Int): Flow<List<Song>> = dao.getSongsInPlaylist(playlistId)

    override suspend fun insert(playlistSong: PlaylistSong) = dao.insert(playlistSong)
    override suspend fun update(playlistSong: PlaylistSong) = dao.update(playlistSong)
    override suspend fun delete(playlistSong: PlaylistSong) = dao.delete(playlistSong)
}