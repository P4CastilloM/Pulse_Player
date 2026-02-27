package com.example.pulseplayer.views.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.pulseplayer.R
import com.example.pulseplayer.data.PulsePlayerDatabase
import com.example.pulseplayer.data.entity.Song
import com.example.pulseplayer.views.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay

@Composable
fun NowPlayingScreen(
    navController: androidx.navigation.NavController,
    songId: Int,
    songIds: List<Int>,
    viewModel: PlayerViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentSong by viewModel.currentSong
    val isPlaying by viewModel.isPlaying
    val isFavorite by viewModel.isCurrentFavorite
    val exoPlayer = viewModel.getPlayer()

    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(1L) }
    var canPlayNext by remember { mutableStateOf(false) }
    var canPlayPrevious by remember { mutableStateOf(false) }
    var showEqDialog by remember { mutableStateOf(false) }

    val equalizer = remember { PlayerEqualizer { viewModel.getPlayer() } }
    DisposableEffect(Unit) {
        onDispose { equalizer.release() }
    }

    LaunchedEffect(currentSong?.idSong) { viewModel.checkIfCurrentSongIsFavorite() }

    LaunchedEffect(songId, songIds) {
        if (viewModel.currentSong.value?.idSong != songId) {
            val dao = PulsePlayerDatabase.getDatabase(context).songDao()
            val songList = songIds.mapNotNull { dao.getById(it) }
            val startIndex = songList.indexOfFirst { it.idSong == songId }
            if (startIndex != -1) viewModel.playPlaylist(songList, startIndex)
        }
    }

    LaunchedEffect(exoPlayer) {
        exoPlayer?.let { player ->
            while (true) {
                currentPosition = player.currentPosition
                duration = player.duration.takeIf { it > 0 } ?: 1L
                canPlayNext = player.hasNextMediaItem()
                canPlayPrevious = player.hasPreviousMediaItem()
                delay(300)
            }
        }
    }

    if (currentSong == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF060911)),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = Color.White) }
        return
    }

    Scaffold(
        containerColor = Color(0xFF060911),
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.toggleShuffleMode() }) {
                    Icon(Icons.Default.Shuffle, null, tint = Color.White.copy(alpha = 0.75f))
                }
                IconButton(onClick = { viewModel.playPrevious() }, enabled = canPlayPrevious) {
                    Icon(Icons.Default.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(34.dp))
                }
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF4F46E5)))),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { if (isPlaying) viewModel.pause() else viewModel.resume() }) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
                IconButton(onClick = { viewModel.playNext() }, enabled = canPlayNext) {
                    Icon(Icons.Default.SkipNext, null, tint = Color.White, modifier = Modifier.size(34.dp))
                }
                IconButton(onClick = { viewModel.toggleRepeatMode() }) {
                    Icon(Icons.Default.Repeat, null, tint = Color.White.copy(alpha = 0.75f))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF090B1A), Color(0xFF05060D))))
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Reproduciendo", color = Color.White, style = MaterialTheme.typography.titleLarge)
                Row {
                    IconButton(onClick = { showEqDialog = true }) {
                        Icon(Icons.Default.Equalizer, contentDescription = "Ecualizador", tint = Color(0xFFA78BFA))
                    }
                    IconButton(onClick = { viewModel.toggleFavoriteStatus() }) {
                        Icon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (isFavorite) Color(0xFFFB7185) else Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Image(
                painter = rememberAsyncImagePainter(
                    model = if (currentSong!!.coverImage?.isEmpty() == true) R.drawable.ic_music_placeholder else currentSong!!.coverImage
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(320.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF111827))
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(currentSong!!.title, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(currentSong!!.artistName, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(24.dp))
            Slider(
                value = currentPosition.toFloat(),
                onValueChange = { exoPlayer?.seekTo(it.toLong()) },
                valueRange = 0f..duration.toFloat()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatDuration(currentPosition), color = Color.White.copy(alpha = 0.7f))
                Text(formatDuration(duration), color = Color.White.copy(alpha = 0.7f))
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showEqDialog) {
        EqualizerDialog(
            equalizer = equalizer,
            onDismiss = { showEqDialog = false }
        )
    }
}

@Composable
private fun EqualizerDialog(equalizer: PlayerEqualizer, onDismiss: () -> Unit) {
    var refreshToken by remember { mutableStateOf(0) }
    val isReady = remember(refreshToken) { equalizer.ensureReady() }
    val presets = remember(refreshToken) { equalizer.presets() }
    var selectedPreset by remember(refreshToken) { mutableStateOf(equalizer.currentPreset()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ecualizador") },
        text = {
            if (!isReady) {
                Text("El ecualizador se habilita cuando el audio está activo. Inicia la reproducción e inténtalo de nuevo.")
            } else {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    Text("Presets", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                        presets.forEach { (presetId, name) ->
                            TextButton(onClick = {
                                equalizer.usePreset(presetId)
                                selectedPreset = presetId
                                refreshToken++
                            }) {
                                Text(
                                    text = name,
                                    color = if (selectedPreset == presetId) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        TextButton(onClick = {
                            selectedPreset = PlayerEqualizer.CUSTOM_PRESET
                            refreshToken++
                        }) {
                            Text(
                                text = "Personalizado",
                                color = if (selectedPreset == PlayerEqualizer.CUSTOM_PRESET) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    val range = equalizer.bandLevelRange()
                    repeat(equalizer.bandCount()) { band ->
                        val freq = equalizer.centerFreqHz(band)
                        var bandLevel by remember(refreshToken, band) { mutableStateOf(equalizer.bandLevel(band).toFloat()) }
                        Text("${freq}Hz", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = bandLevel,
                            onValueChange = {
                                bandLevel = it
                                equalizer.setBandLevel(band, it.toInt().toShort())
                                selectedPreset = PlayerEqualizer.CUSTOM_PRESET
                            },
                            valueRange = range.start.toFloat()..range.endInclusive.toFloat()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Cerrar")
            }
        }
    )
}

fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%02d:%02d", min, sec)
}
