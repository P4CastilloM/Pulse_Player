package com.example.pulseplayer.views.player


import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.pulseplayer.data.entity.Song

object ExoPlayerManager {
    private var exoPlayer: ExoPlayer? = null
    private var currentSong: Song? = null

    fun init(context: Context) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
        }
    }

    fun play(song: Song) {
        if (currentSong?.idSong == song.idSong) return
        stop()
        currentSong = song
        val mediaItem = MediaItem.fromUri(song.filePath)
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
