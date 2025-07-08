package com.example.pulseplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.pulseplayer.views.player.ExoPlayerManager

@UnstableApi
class MediaPlaybackService : MediaSessionService(){

    companion object {
        private const val NOTIFICATION_ID = 101
        private const val CHANNEL_ID = "pulseplayer_channel"
    }

    private lateinit var mediaSession: MediaSession

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        //obtener reproductor
        val player = ExoPlayerManager.getPlayer()!!

        mediaSession = MediaSession.Builder(this, player)
            .setId("pulseplayer_session")
            .build()

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .setChannelId(CHANNEL_ID)
                .build()
        )

        if (checkSelfPermission(android.Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK) ==
            PackageManager.PERMISSION_GRANTED) {
            player.addListener(playbackListener)
        } else {
            stopSelf() // Detener el servicio si no hay permisos
        }

    }

    private val playbackListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                ensureForeground()
            }
        }
    }

    private fun ensureForeground() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(androidx.media3.session.R.drawable.media_session_service_notification_ic_music_note)
            .setContentTitle(mediaSession.player.currentMediaItem?.mediaMetadata?.title ?: "PulsePlayer")
            .setContentText(mediaSession.player.currentMediaItem?.mediaMetadata?.artist ?: "Reproduciendo música")
            .setPriority(NotificationCompat.PRIORITY_LOW) // Importante para Android 7.1 y anteriores
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Visible en pantalla bloqueada
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId) // <-- Llamada crítica
        if (::mediaSession.isInitialized && mediaSession.player.playbackState == Player.STATE_READY) {
            ensureForeground()
        }
        return START_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    override fun onDestroy() {
        if (::mediaSession.isInitialized) {
            mediaSession.release()
        }
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reproducción de música",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controles de reproducción"
                setShowBadge(false)
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }
}