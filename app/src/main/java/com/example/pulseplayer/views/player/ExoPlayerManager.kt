package com.example.pulseplayer.views.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.Player // Importa Player.Listener
import com.example.pulseplayer.data.entity.Song // Asegúrate de que tu clase Song esté aquí

object ExoPlayerManager {
    private var exoPlayer: ExoPlayer? = null
    // Estas variables ahora gestionarán la lista de reproducción actual y la canción actual
    private var currentPlaylist: List<Song> = emptyList()
    private var currentPlaylistIndex: Int = -1
    private var _currentSong: Song? = null // Usamos una variable interna para la canción actual
    private var lastSavedMediaId: String? = null

    private var saveToHistoryCallback: ((Song) -> Unit)? = null

    fun setOnSaveToHistoryCallback(callback: (Song) -> Unit) {
        saveToHistoryCallback = callback
    }

    // Listener para escuchar eventos de ExoPlayer y mantener nuestro estado sincronizado
    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val newIndex = exoPlayer?.currentMediaItemIndex ?: -1
            if (newIndex != -1 && newIndex < currentPlaylist.size) {
                currentPlaylistIndex = newIndex
                val newSong = currentPlaylist[currentPlaylistIndex]
                _currentSong = newSong

                // Guardar en historial solo si es un mediaId nuevo
                val mediaId = mediaItem?.mediaId
                if (mediaId != null && mediaId != lastSavedMediaId) {
                    saveToHistoryCallback?.invoke(newSong)
                    lastSavedMediaId = mediaId
                }

            } else {
                _currentSong = null
            }
        }


        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                // Si la reproducción llega al final de la cola de ExoPlayer,
                // aseguramos que nuestro estado interno refleje que no hay más canciones.
                if (exoPlayer?.currentMediaItem == null) {
                    // La lista ha terminado, limpiamos el estado
                    currentPlaylist = emptyList()
                    currentPlaylistIndex = -1
                    _currentSong = null
                }
            }
        }
    }

    /**
     * Inicializa la instancia de ExoPlayer. Solo se ejecuta una vez.
     * @param context Contexto de la aplicación.
     */
    fun init(context: Context) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
            exoPlayer?.addListener(playerListener) // ¡Importante! Añadir el listener aquí
        }
    }

    /**
     * Inicia la reproducción de una lista de canciones desde un índice específico.
     * Esto REEMPLAZA completamente la cola de medios anterior en ExoPlayer.
     * @param songs La lista de canciones a reproducir.
     * @param startIndex El índice de la canción desde la que empezar.
     */
    fun playPlaylist(songs: List<Song>, startIndex: Int) {
        if (exoPlayer == null) {
            // El reproductor no está inicializado, asegúrate de que 'init()' se llamó.
            return
        }

        // Actualiza el estado interno del manager con la nueva lista y el índice
        currentPlaylist = songs
        currentPlaylistIndex = startIndex
        _currentSong = if (startIndex >= 0 && startIndex < songs.size) songs[startIndex] else null

        // Mapea tus objetos Song a MediaItem de ExoPlayer
        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setUri(song.filePath) // Asegúrate de que 'filePath' es la URI del audio
                .setMediaId(song.idSong.toString()) // Opcional, para identificar el item
                .build()
        }

        // ¡CLAVE! Esto reemplaza la cola de ExoPlayer con la nueva lista.
        // El 0L indica la posición de inicio dentro de la primera canción (desde el principio).
        exoPlayer?.setMediaItems(mediaItems, startIndex, 0L)
        exoPlayer?.prepare() // Prepara la reproducción
        exoPlayer?.play() // Comienza a reproducir
    }

    /**
     * Reproduce una única canción. Esto la trata como una lista de reproducción de un solo elemento.
     * @param song La canción a reproducir.
     */
    fun play(song: Song) {
        // Si ya se está reproduciendo esta canción y el reproductor está activo, no hacemos nada.
        if (_currentSong?.idSong == song.idSong && exoPlayer?.isPlaying == true) {
            return
        }
        // Llama a playPlaylist para que maneje el reemplazo de la cola con una lista de un solo elemento.
        playPlaylist(listOf(song), 0)
    }

    /**
     * Detiene la reproducción y limpia la cola de medios.
     */
    fun stop() {
        exoPlayer?.stop()
        exoPlayer?.clearMediaItems() // Asegura que la cola de medios de ExoPlayer esté vacía
        // Limpia también el estado interno del manager
        currentPlaylist = emptyList()
        currentPlaylistIndex = -1
        _currentSong = null
    }

    /**
     * Pausa la reproducción actual.
     */
    fun pause() = exoPlayer?.pause()

    /**
     * Reanuda la reproducción actual.
     */
    fun resume() = exoPlayer?.play()

    /**
     * Avanza a la siguiente canción en la lista de reproducción.
     */
    fun playNext() {
        if (exoPlayer?.hasNextMediaItem() == true) {
            exoPlayer?.seekToNextMediaItem()
        } else {
            // Si no hay siguiente canción en la cola de ExoPlayer, detenemos y limpiamos.
            stop()
        }
    }

    /**
     * Retrocede a la canción anterior en la lista de reproducción.
     */
    fun playPrevious() {
        if (exoPlayer?.hasPreviousMediaItem() == true) {
            exoPlayer?.seekToPreviousMediaItem()
        } else {
            // Si no hay canción anterior, detenemos y limpiamos.
            stop()
        }
    }

    /**
     * Libera todos los recursos de ExoPlayer. Debe llamarse cuando el reproductor ya no es necesario.
     */
    fun release() {
        exoPlayer?.removeListener(playerListener) // Quita el listener antes de liberar
        exoPlayer?.release()
        exoPlayer = null
        // Limpia el estado interno del manager al liberar
        currentPlaylist = emptyList()
        currentPlaylistIndex = -1
        _currentSong = null
    }

    /**
     * Obtiene la instancia de ExoPlayer.
     * @return La instancia de ExoPlayer o null si no está inicializada.
     */
    fun getPlayer() = exoPlayer

    /**
     * Obtiene la canción que se está reproduciendo actualmente según el manager.
     * @return La canción actual o null si no hay ninguna.
     */
    fun getCurrentSong() = _currentSong
    fun getCurrentSongId(): Int? {
        return exoPlayer?.currentMediaItem?.mediaId?.toIntOrNull()
    }
}