package com.example.pulseplayer.views.player


import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.pulseplayer.MediaPlaybackService
import com.example.pulseplayer.data.entity.Song

object ExoPlayerManager {
    private var exoPlayer: ExoPlayer? = null
    private var currentSong: Song? = null
    private var appContext: Context? = null

    fun init(context: Context) {
        if (exoPlayer == null) {
            appContext = context.applicationContext
            exoPlayer = ExoPlayer.Builder(appContext!!).build()
        }
    }

    private fun getContext(): Context{
        return appContext ?: throw IllegalStateException("ExoPlayerManager no está inicializado. Llama init() primero.")
    }

    @OptIn(UnstableApi::class)
    fun play(song: Song) {
        val intent = Intent(getContext(), MediaPlaybackService::class.java)
        ContextCompat.startForegroundService(getContext(), intent)
        if (exoPlayer == null) throw IllegalStateException("ExoPlayerManager no está inicializado. Llama init() primero.")
        if (currentSong?.idSong == song.idSong) return

        stop()
        currentSong = song
//        val mediaItem = MediaItem.fromUri(song.filePath)
        val mediaItem = MediaItem.Builder()
            .setUri(song.filePath)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artistName)
                    .setArtworkUri(song.coverImage?.toUri()) // opcional
                    .build()
            )
            .build()

        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()
        exoPlayer?.play()
    }

    fun stop() {
        exoPlayer?.stop()
        exoPlayer?.clearMediaItems()
    }

    fun pause() = exoPlayer?.pause()
    fun resume() = exoPlayer?.play()
    fun release() = exoPlayer?.release()

    fun getPlayer() = exoPlayer
    fun getCurrentSong() = currentSong
}
