package com.example.pulseplayer.views.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.example.pulseplayer.MainActivity
import com.example.pulseplayer.R
import com.example.pulseplayer.views.player.ExoPlayerManager
import java.io.File

class MusicPlayerService : Service() {

    override fun onCreate() {
        super.onCreate()
        Log.d("MusicPlayerService", "✅ onCreate llamado")
        // ✅ accede al player correctamente
        ExoPlayerManager.getPlayer()?.addListener(object : Player.Listener {

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                showCustomNotification()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                showCustomNotification()
            }
        })
        createNotificationChannel()
        showCustomNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "ACTION_PLAY" -> ExoPlayerManager.resume()
            "ACTION_PAUSE" -> ExoPlayerManager.pause()
            "ACTION_NEXT" -> ExoPlayerManager.playNext()
            "ACTION_PREV" -> ExoPlayerManager.playPrevious()
            "ACTION_STOP" -> {
                ExoPlayerManager.pause()
                stopForeground(true)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        showCustomNotification()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MusicPlayerService", "🛑 Servicio detenido")
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "pulseplayer_channel",
                "PulsePlayer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Control de reproducción de PulsePlayer"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun showCustomNotification() {
        val player = ExoPlayerManager.getPlayer()
        val song = ExoPlayerManager.getCurrentSong() ?: return
        val isPlaying = player?.isPlaying == true

        // Intents para los botones
        val playPauseIntent = Intent(this, MusicPlayerService::class.java).apply {
            action = if (isPlaying) "ACTION_PAUSE" else "ACTION_PLAY"
        }
        val prevIntent = Intent(this, MusicPlayerService::class.java).apply {
            action = "ACTION_PREV"
        }
        val nextIntent = Intent(this, MusicPlayerService::class.java).apply {
            action = "ACTION_NEXT"
        }
        val stopIntent = Intent(this, MusicPlayerService::class.java).apply {
            action = "ACTION_STOP"
        }

        // PendingIntents
        val playPausePendingIntent = PendingIntent.getService(this, 0, playPauseIntent, PendingIntent.FLAG_IMMUTABLE)
        val prevPendingIntent = PendingIntent.getService(this, 1, prevIntent, PendingIntent.FLAG_IMMUTABLE)
        val nextPendingIntent = PendingIntent.getService(this, 2, nextIntent, PendingIntent.FLAG_IMMUTABLE)
        val stopPendingIntent = PendingIntent.getService(this, 3, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        // RemoteViews personalizados
        val remoteViews = RemoteViews(packageName, R.layout.notification_music_player).apply {
            setTextViewText(R.id.text_title, song.title.ifEmpty { "Título desconocido" })
            setTextViewText(R.id.text_artist, song.artistName.ifEmpty { "Artista desconocido" })

            // Cargar portada desde URI
            val bitmap = getBitmapFromUri(song.coverImage)
            if (bitmap != null) {
                setImageViewBitmap(R.id.image_cover, bitmap)
            } else {
                setImageViewResource(R.id.image_cover, R.drawable.ic_music_placeholder)
            }

            // Botón play/pause
            setImageViewResource(
                R.id.btn_play_pause,
                if (isPlaying) R.drawable.pause2_icon else R.drawable.play2_icon
            )
            setOnClickPendingIntent(R.id.btn_play_pause, playPausePendingIntent)

            // Botones anteriores/siguiente/cerrar
            setImageViewResource(R.id.btn_prev, R.drawable.back2_icon)
            setImageViewResource(R.id.btn_next, R.drawable.skip2_icon)
            setImageViewResource(R.id.btn_close, R.drawable.stop_icon)

            setOnClickPendingIntent(R.id.btn_prev, prevPendingIntent)
            setOnClickPendingIntent(R.id.btn_next, nextPendingIntent)
            setOnClickPendingIntent(R.id.btn_close, stopPendingIntent)
        }

        // Construcción de la notificación
        val notification = NotificationCompat.Builder(this, "pulseplayer_channel")
            .setSmallIcon(R.drawable.logo_ico)
            .setColor(ContextCompat.getColor(this, R.color.pulse_blue))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteViews)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

        startForeground(1, notification)
    }

    private fun getBitmapFromUri(uriString: String?): Bitmap? {
        if (uriString.isNullOrEmpty()) return null
        return try {
            val uri = Uri.parse(uriString)
            contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



}
