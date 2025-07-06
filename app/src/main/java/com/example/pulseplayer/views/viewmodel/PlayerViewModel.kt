package com.example.pulseplayer.views.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.pulseplayer.data.entity.Song
import com.example.pulseplayer.data.PulsePlayerDatabase
import com.example.pulseplayer.data.entity.PlaybackHistory
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    private var currentIndex = 0
    private var playlist: List<Song> = emptyList()

    private val _currentSong = mutableStateOf<Song?>(null)
    val currentSong: State<Song?> get() = _currentSong

    private val _isPlaying = mutableStateOf(false)
    val isPlaying: State<Boolean> get() = _isPlaying

    private val db = PulsePlayerDatabase.getDatabase(context)
    private val historyDao = db.playbackHistoryDao()
    private val viewModelScopeIO = CoroutineScope(Dispatchers.IO + SupervisorJob()) // para operaciones Room

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                println("Player state changed: $state")
                if (state == Player.STATE_ENDED) {
                    println("Canción terminó, reproduciendo siguiente...")
                    playNext()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
        })
    }

    fun playSong(song: Song) {
        _currentSong.value = song
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        val mediaItem = MediaItem.fromUri(Uri.parse(song.filePath))
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

        // 🔁 Guardar en historial
        saveToHistory(song)
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
    private fun saveToHistory(song: Song) {
        viewModelScopeIO.launch {
            // 1. Verificar y limpiar si hay más de 50 registros
            val count = historyDao.getCount()
            if (count >= 50) {
                historyDao.deleteOldest(count - 49) // mantener solo los 49 más nuevos, luego se insertará 1 más
            }

            // 2. Obtener la fecha actual como string
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            // 3. Insertar nuevo registro
            val history = PlaybackHistory(songId = song.idSong, playedAt = now)
            historyDao.insert(history)
        }
    }

    fun pause() = exoPlayer.pause()
    fun resume() = exoPlayer.play()
    fun isPlaying() = exoPlayer.isPlaying
    fun release() = exoPlayer.release()
    fun getPlayer() = exoPlayer
}
