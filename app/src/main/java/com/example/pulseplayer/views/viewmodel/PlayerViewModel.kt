package com.example.pulseplayer.views.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.pulseplayer.data.entity.Song

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    private var currentIndex = 0
    private var playlist: List<Song> = emptyList()

    private val _currentSong = mutableStateOf<Song?>(null)
    val currentSong: State<Song?> get() = _currentSong


    fun playSong(song: Song) {
        _currentSong.value = song
        val mediaItem = MediaItem.fromUri(Uri.parse(song.filePath))
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun playPlaylist(songs: List<Song>, startIndex: Int) {
        playlist = songs
        currentIndex = startIndex
        playSong(playlist[currentIndex])
    }

    fun playNext() {
        if (playlist.isNotEmpty() && currentIndex + 1 < playlist.size) {
            currentIndex++
            playSong(playlist[currentIndex])
        }
    }

    fun playPrevious() {
        if (playlist.isNotEmpty() && currentIndex > 0) {
            currentIndex--
            playSong(playlist[currentIndex])
        }
    }

    fun pause() = exoPlayer.pause()
    fun resume() = exoPlayer.play()
    fun isPlaying() = exoPlayer.isPlaying
    fun release() = exoPlayer.release()
    fun getPlayer() = exoPlayer
}
