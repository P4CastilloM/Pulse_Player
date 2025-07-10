package com.example.pulseplayer

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.app.NotificationManager
import android.content.res.Configuration
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.pulseplayer.data.PulsePlayerDatabase
import com.example.pulseplayer.data.entity.Song
import com.example.pulseplayer.ui.theme.PulsePlayerTheme
import com.example.pulseplayer.util.MusicScanner
import com.example.pulseplayer.views.AppLifecycleObserver
import com.example.pulseplayer.views.player.ExoPlayerManager
import com.example.pulseplayer.views.service.MusicPlayerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = PulsePlayerDatabase.getDatabase(applicationContext)

        // 👉 Solicita permisos necesarios al iniciar
        requestAllPermissions()

        // 👉 Observador del ciclo de vida para activar/desactivar la notificación
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
                    manager.cancel(1) // Oculta la notificación al volver a la app
                }
            )
        )

        enableEdgeToEdge()
        setContent {
            PulsePlayerTheme {
                PulsePlayerApp(database)
            }
        }
    }

    // ✅ Pide todos los permisos necesarios (según la versión de Android)
    private fun requestAllPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(android.Manifest.permission.READ_MEDIA_AUDIO)
            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 100)
        }
    }
}

@Composable
fun PulsePlayerApp(database: PulsePlayerDatabase) {
    val context = LocalContext.current
    val songsState = remember { mutableStateOf<List<Song>>(emptyList()) }

    LaunchedEffect(Unit) {
        var retries = 10
        while (!hasStoragePermission(context) && retries > 0) {
            kotlinx.coroutines.delay(500)
            retries--
        }

        if (!hasStoragePermission(context)) {
            Log.e("PulsePlayer", "❌ Permiso de almacenamiento NO concedido")
            return@LaunchedEffect
        }

        try {
            database.openHelper.writableDatabase
            withContext(Dispatchers.IO) {
                val dao = database.songDao()
                MusicScanner.scanAndSyncSongs(context, dao)
                songsState.value = dao.getAll().first()
                Log.d("PulsePlayer", "✅ Canciones escaneadas correctamente")
            }
        } catch (e: Exception) {
            Log.e("PulsePlayer", "❌ Error escaneando canciones: ${e.message}")
        }
    }

    PulsePlayerTheme {
        Navigation(songs = songsState.value) // ✅ se pasan aquí
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

@Composable
fun isLandscape(): Boolean{
    val config = LocalConfiguration.current
    return config.orientation == Configuration.ORIENTATION_LANDSCAPE
}
