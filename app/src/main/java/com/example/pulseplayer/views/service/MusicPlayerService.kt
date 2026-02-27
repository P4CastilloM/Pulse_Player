package com.example.pulseplayer.views.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.example.pulseplayer.MainActivity
import com.example.pulseplayer.R
import com.example.pulseplayer.views.player.ExoPlayerManager

class MusicPlayerService : Service() {

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            showPlayerNotification()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            showPlayerNotification()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            showPlayerNotification()
        }
    }

    override fun onCreate() {
        super.onCreate()
        ExoPlayerManager.init(applicationContext)
        ExoPlayerManager.getPlayer()?.addListener(playerListener)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> ExoPlayerManager.resume()
            ACTION_PAUSE -> ExoPlayerManager.pause()
            ACTION_NEXT -> ExoPlayerManager.playNext()
            ACTION_PREV -> ExoPlayerManager.playPrevious()
            ACTION_STOP -> {
                ExoPlayerManager.pause()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_SYNC, null -> Unit
        }

        showPlayerNotification()
        return START_STICKY
    }

    override fun onDestroy() {
        ExoPlayerManager.getPlayer()?.removeListener(playerListener)
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "PulsePlayer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controles de reproducción"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun showPlayerNotification() {
        val song = ExoPlayerManager.getCurrentSong()
        val isPlaying = ExoPlayerManager.getPlayer()?.isPlaying == true

        if (song == null) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        val openAppIntent = PendingIntent.getActivity(
            this,
            20,
            Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val prevIntent = actionIntent(ACTION_PREV, 1)
        val playPauseIntent = actionIntent(if (isPlaying) ACTION_PAUSE else ACTION_PLAY, 2)
        val nextIntent = actionIntent(ACTION_NEXT, 3)
        val stopIntent = actionIntent(ACTION_STOP, 4)

        val artwork = getBitmapFromUri(song.coverImage)

        val customView = RemoteViews(packageName, R.layout.notification_music_player).apply {
            setTextViewText(R.id.text_title, song.title.ifBlank { "Pulse Player" })
            setTextViewText(R.id.text_artist, song.artistName.ifBlank { "Artista desconocido" })
            setImageViewResource(
                R.id.btn_play_pause,
                if (isPlaying) R.drawable.ic_notif_pause else R.drawable.ic_notif_play
            )

            if (artwork != null) {
                setImageViewBitmap(R.id.image_cover, artwork)
            } else {
                setImageViewResource(R.id.image_cover, R.drawable.ic_music_placeholder)
            }

            setOnClickPendingIntent(R.id.notification_layout, openAppIntent)
            setOnClickPendingIntent(R.id.btn_prev, prevIntent)
            setOnClickPendingIntent(R.id.btn_play_pause, playPauseIntent)
            setOnClickPendingIntent(R.id.btn_next, nextIntent)
            setOnClickPendingIntent(R.id.btn_close, stopIntent)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_ico)
            .setColor(ContextCompat.getColor(this, R.color.pulse_blue))
            .setContentIntent(openAppIntent)
            .setDeleteIntent(stopIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setOngoing(isPlaying)
            .setCustomContentView(customView)
            .setCustomBigContentView(customView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun actionIntent(action: String, requestCode: Int): PendingIntent {
        return PendingIntent.getService(
            this,
            requestCode,
            Intent(this, MusicPlayerService::class.java).setAction(action),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getBitmapFromUri(uriString: String?): Bitmap? {
        if (uriString.isNullOrEmpty()) return null
        return try {
            val uri = Uri.parse(uriString)
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val CHANNEL_ID = "pulseplayer_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_NEXT = "ACTION_NEXT"
        const val ACTION_PREV = "ACTION_PREV"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_SYNC = "ACTION_SYNC"

        fun start(context: Context, action: String = ACTION_SYNC) {
            val intent = Intent(context, MusicPlayerService::class.java).setAction(action)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
