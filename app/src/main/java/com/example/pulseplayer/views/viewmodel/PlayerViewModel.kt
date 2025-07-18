package com.example.pulseplayer.views.viewmodel

import android.app.Application
import android.content.Intent
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.example.pulseplayer.data.PulsePlayerDatabase
import com.example.pulseplayer.data.entity.PlaybackHistory
import com.example.pulseplayer.data.entity.Song
import com.example.pulseplayer.views.player.ExoPlayerManager // Asegúrate de que esta importación sea correcta
import com.example.pulseplayer.views.service.MusicPlayerService
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

    // Estas variables internas del ViewModel ahora reflejarán el estado del ExoPlayerManager
    private val _currentSong = mutableStateOf<Song?>(null)
    val currentSong: State<Song?> get() = _currentSong

    private val _isPlaying = mutableStateOf(false)
    val isPlaying: State<Boolean> get() = _isPlaying

    // Variables para controlar el guardado en el historial y evitar duplicados
    //private var _lastSavedSongId: Int? = null
    // No necesitamos _lastSaveTimestamp ni SAVE_DEBOUNCE_MS si el guardado es más preciso.

    // Listener para escuchar eventos de ExoPlayer a través de ExoPlayerManager
    private val playerStateListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            // La lógica de guardado en historial se ha movido a onMediaItemTransition para mayor fiabilidad.
            // Si la canción ya fue guardada en onMediaItemTransition, no la guardamos de nuevo aquí.
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            // Cuando ExoPlayer transiciona a un nuevo elemento, obtenemos la canción actual del manager.
            val newSong = ExoPlayerManager.getCurrentSong()
            _currentSong.value = newSong

            // ¡ACTUALIZADO! Guarda la canción en el historial SOLO si es una canción diferente
            // a la última guardada, para evitar duplicados en transiciones rápidas o reinicios.
           //newSong?.let { song ->
           //    if (song.idSong != _lastSavedSongId) {
           //        saveToHistory(song)
           //        _lastSavedSongId = song.idSong // Actualiza la última canción guardada
           //    }
           //}
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                // Si la reproducción llega al final de la cola de ExoPlayer
                // y no hay más elementos según el manager, actualizamos la UI.
                if (ExoPlayerManager.getPlayer()?.currentMediaItem == null) {
                    _currentSong.value = null
                    _isPlaying.value = false
                    //_lastSavedSongId = null // Resetea la última canción guardada al finalizar la playlist
                }
            }
        }
    }

    init {
        ExoPlayerManager.init(context)

        // Registra el callback cuando cambia la canción dentro del ExoPlayerManager
        ExoPlayerManager.setOnSaveToHistoryCallback { song ->
            saveToHistory(song)
        }

        ExoPlayerManager.getPlayer()?.removeListener(playerStateListener)
        ExoPlayerManager.getPlayer()?.addListener(playerStateListener)
    }


    /**
     * Inicia la reproducción de una única canción.
     * Delega la lógica de reproducción y gestión de cola a ExoPlayerManager.
     * @param song La canción a reproducir.
     */
    fun playSong(song: Song) {
        ExoPlayerManager.play(song) // primero comienza la reproducción
        _currentSong.value = song

       //// Luego inicia el servicio
       //val intent = Intent(context, MusicPlayerService::class.java)
       //ContextCompat.startForegroundService(context, intent)
    }



    /**
     * Inicia la reproducción de una lista de canciones desde un índice específico.
     * Delega la lógica de reproducción y gestión de cola a ExoPlayerManager.
     * @param songs La lista de canciones a reproducir.
     * @param startIndex El índice de la canción desde la que empezar.
     */
    fun playPlaylist(songs: List<Song>, startIndex: Int) {
        ExoPlayerManager.playPlaylist(songs, startIndex)
        _currentSong.value = songs[startIndex]
       // startMusicService() // Esto ya está bien, solo asegúrate que se llama después de play
    }



    /**
     * Avanza a la siguiente canción en la lista de reproducción.
     * Delega la acción a ExoPlayerManager.
     */
    fun playNext() {
        ExoPlayerManager.playNext()
    }

    /**
     * Retrocede a la canción anterior en la lista de reproducción.
     * Delega la acción a ExoPlayerManager.
     */
    fun playPrevious() {
        ExoPlayerManager.playPrevious()
    }

    /**
     * Pausa la reproducción actual.
     * Delega la acción a ExoPlayerManager.
     */
    fun pause() = ExoPlayerManager.pause()

    /**
     * Reanuda la reproducción actual.
     * Delega la acción a ExoPlayerManager.
     */
    fun resume() = ExoPlayerManager.resume()

    /**
     * Verifica si el reproductor está actualmente reproduciendo.
     * Obtiene el estado de reproducción del manager.
     * @return true si está reproduciendo, false en caso contrario.
     */
    fun isPlaying() = _isPlaying.value // Usa el estado observado del ViewModel

    /**
     * Libera los recursos del reproductor.
     * Este método es llamado automáticamente cuando el ViewModel ya no es necesario.
     */
    /*override fun onCleared() {
        super.onCleared()
        // Quita el listener del reproductor para evitar fugas de memoria
        ExoPlayerManager.getPlayer()?.removeListener(playerStateListener)
        // Llama a release en ExoPlayerManager para liberar los recursos del reproductor
        // Esto es crucial para que el ExoPlayer se libere cuando el ViewModel ya no se use.
        ExoPlayerManager.release()
    }*/

    /**
     * Obtiene la instancia de ExoPlayer desde el manager.
     * @return La instancia de ExoPlayer o null.
     */
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

    // Favorito
    private val _isCurrentFavorite = mutableStateOf(false)
    val isCurrentFavorite: State<Boolean> get() = _isCurrentFavorite

    fun checkIfCurrentSongIsFavorite() {
        viewModelScope.launch {
            val song = currentSong.value ?: return@launch
            val dao = PulsePlayerDatabase.getDatabase(getApplication()).songDao()
            _isCurrentFavorite.value = dao.getById(song.idSong)?.isFavorite == true
        }
    }

    fun toggleFavoriteStatus() {
        viewModelScope.launch {
            val song = currentSong.value ?: return@launch
            val dao = PulsePlayerDatabase.getDatabase(getApplication()).songDao()
            val updatedSong = song.copy(isFavorite = !isCurrentFavorite.value)
            dao.update(updatedSong)
            _isCurrentFavorite.value = updatedSong.isFavorite
        }
    }


    //Repetición
    private val _isRepeatEnabled = mutableStateOf(false)
    val isRepeatEnabled: State<Boolean> get() = _isRepeatEnabled

    fun toggleRepeatMode() {
        val player = ExoPlayerManager.getPlayer() ?: return
        _isRepeatEnabled.value = !_isRepeatEnabled.value
        player.repeatMode = if (_isRepeatEnabled.value) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
    }

    //Aleatorio
    private val _isShuffleEnabled = mutableStateOf(false)
    val isShuffleEnabled: State<Boolean> get() = _isShuffleEnabled

    fun toggleShuffleMode() {
        val player = ExoPlayerManager.getPlayer() ?: return
        _isShuffleEnabled.value = !_isShuffleEnabled.value
        player.shuffleModeEnabled = _isShuffleEnabled.value
    }
    init {
        // ✅ Esto sincroniza el estado inicial con el reproductor
        ExoPlayerManager.getPlayer()?.let { player ->
            _isRepeatEnabled.value = player.repeatMode == Player.REPEAT_MODE_ALL
            _isShuffleEnabled.value = player.shuffleModeEnabled
        }
    }

    private fun startMusicService() {
        val intent = Intent(context, MusicPlayerService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

}