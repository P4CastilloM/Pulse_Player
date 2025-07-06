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

