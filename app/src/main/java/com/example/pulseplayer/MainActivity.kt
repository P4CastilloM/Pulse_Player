package com.example.pulseplayer

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pulseplayer.data.PulsePlayerDatabase
import com.example.pulseplayer.ui.theme.PulsePlayerTheme
import com.example.pulseplayer.util.MusicScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔒 Permisos de almacenamiento
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

        // 🔔 Permiso de notificaciones (solo Android 13+)
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
