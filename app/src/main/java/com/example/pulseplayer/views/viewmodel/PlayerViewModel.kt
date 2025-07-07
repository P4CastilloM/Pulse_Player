package com.example.pulseplayer.views.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.Player
import com.example.pulseplayer.data.PulsePlayerDatabase
import com.example.pulseplayer.data.entity.PlaybackHistory
import com.example.pulseplayer.data.entity.Song
import com.example.pulseplayer.views.player.ExoPlayerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val db = PulsePlayerDatabase.getDatabase(context)
    private val historyDao = db.playbackHistoryDao()
    private val viewModelScopeIO = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var currentIndex = 0
    private var playlist: List<Song> = emptyList()

    private val _currentSong = mutableStateOf<Song?>(null)
    val currentSong: State<Song?> get() = _currentSong

    private val _isPlaying = mutableStateOf(false)
    val isPlaying: State<Boolean> get() = _isPlaying

    init {
        ExoPlayerManager.init(context)

        ExoPlayerManager.getPlayer()?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    playNext()
                }
            }
        })
    }

    fun playSong(song: Song, saveHistory: Boolean = true) {
        _currentSong.value = song
        ExoPlayerManager.play(song)
        if (saveHistory) {
            saveToHistory(song)
        }
    }


    fun playPlaylist(songs: List<Song>, startIndex: Int, saveHistory: Boolean = true) {
        playlist = songs
        currentIndex = startIndex
        playSong(playlist[currentIndex], saveHistory)
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

    fun pause() = ExoPlayerManager.pause()
    fun resume() = ExoPlayerManager.resume()
    fun isPlaying() = ExoPlayerManager.getPlayer()?.isPlaying ?: false
    fun release() = ExoPlayerManager.release()
    fun getPlayer() = ExoPlayerManager.getPlayer()

    private fun saveToHistory(song: Song) {
        viewModelScopeIO.launch {
            val count = historyDao.getCount()
            if (count >= 50) {
                historyDao.deleteOldest(count - 49)
            }
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val history = PlaybackHistory(songId = song.idSong, playedAt = now)
            historyDao.insert(history)
        }
    }
}
