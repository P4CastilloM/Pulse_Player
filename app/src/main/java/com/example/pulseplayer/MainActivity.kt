package com.example.pulseplayer

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.pulseplayer.data.PulsePlayerDatabase
import com.example.pulseplayer.ui.theme.PulsePlayerTheme
import com.example.pulseplayer.util.MusicScanner
import com.example.pulseplayer.views.player.ExoPlayerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.app.NotificationManager
import com.example.pulseplayer.views.AppLifecycleObserver
import com.example.pulseplayer.views.service.MusicPlayerService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permisos de almacenamiento
        if (!hasStoragePermission(this)) {
            ActivityCompat.requestPermissions(
                this,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO)
                else
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                100
            )
        }

        // Permiso de notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }

        // 👇 REGISTRA EL OBSERVADOR DE CICLO DE VIDA
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            AppLifecycleObserver(
                onAppBackgrounded = {
                    if (ExoPlayerManager.getPlayer()?.isPlaying == true) {
                        val intent = Intent(this, MusicPlayerService::class.java)
                        ContextCompat.startForegroundService(this, intent)
                    }
                },
                onAppForegrounded = {
                    val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    manager.cancel(1) // ID de la notificación
                }
            )
        )

        enableEdgeToEdge()
        setContent {
            PulsePlayerTheme {
                PulsePlayerApp()
            }
        }
    }
}

@Composable
fun PulsePlayerApp() {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (hasStoragePermission(context)) {
            withContext(Dispatchers.IO) {
                val dao = PulsePlayerDatabase.getDatabase(context).songDao()
                MusicScanner.scanAndSyncSongs(context, dao)
            }
        }
    }

    PulsePlayerTheme {
        Navigation()
    }
}

private fun hasStoragePermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_MEDIA_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
}
