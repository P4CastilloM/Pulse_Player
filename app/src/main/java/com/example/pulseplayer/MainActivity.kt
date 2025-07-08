package com.example.pulseplayer

import android.content.Context
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
import com.example.pulseplayer.data.PulsePlayerDatabase
import com.example.pulseplayer.ui.theme.PulsePlayerTheme
import com.example.pulseplayer.util.MusicScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Permisos
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    200
                )
            }
        }

        // Nuevo: Permiso para reproducción en foreground (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK),
                    300  // Nuevo request code
                )
            }
        }

        enableEdgeToEdge()
        setContent {
            PulsePlayerTheme {
                PulsePlayerApp()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> { /* Manejo permiso almacenamiento existente */ }
            200 -> { /* Manejo permiso notificaciones existente */ }
            300 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso de reproducción en foreground concedido
                }
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
                // 🔥 Importante: escanea en hilo secundario
                MusicScanner.scanAndSyncSongs(context, dao)
            }
        }
    }

    PulsePlayerTheme {
        Navigation()
    }

    // https://drive.google.com/drive/folders/1dF4tz8xeaFm5Tjr6pX7K-zYjysG73Fww?usp=sharing  MUSICA
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

