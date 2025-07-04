package com.example.pulseplayer.data

import android.content.Context
import com.example.pulseplayer.data.offlineRepository.OfflineSongRepository
import com.example.pulseplayer.data.offlineRepository.OfflinePlaylistRepository
import com.example.pulseplayer.data.offlineRepository.OfflinePlaylistSongRepository
import com.example.pulseplayer.data.offlineRepository.OfflinePlaybackHistoryRepository
import com.example.pulseplayer.data.repository.SongRepository
import com.example.pulseplayer.data.repository.PlaylistRepository
import com.example.pulseplayer.data.repository.PlaylistSongRepository
import com.example.pulseplayer.data.repository.PlaybackHistoryRepository

class AppContainer(context: Context) {

    private val db = PulsePlayerDatabase.getDatabase(context)

    val songRepository: SongRepository = OfflineSongRepository(db.songDao())

    val playlistRepository: PlaylistRepository = OfflinePlaylistRepository(db.playlistDao())

    val playlistSongRepository: PlaylistSongRepository = OfflinePlaylistSongRepository(db.playlistSongDao())

    val playbackHistoryRepository: PlaybackHistoryRepository = OfflinePlaybackHistoryRepository(db.playbackHistoryDao())
}

