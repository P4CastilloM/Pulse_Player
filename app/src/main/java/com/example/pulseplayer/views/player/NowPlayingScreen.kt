package com.example.pulseplayer.views.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
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
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pulseplayer.R
import com.example.pulseplayer.data.PulsePlayerDatabase
import com.example.pulseplayer.views.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay

@Composable
fun NowPlayingScreen(
    navController: NavController,
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
    DisposableEffect(Unit) { onDispose { equalizer.release() } }

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

    val song = currentSong
    if (song == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF060911)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    Scaffold(
        containerColor = Color(0xFF05060D),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.28f))
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF0A0A14), Color(0xFF090B1A), Color(0xFF06070F))))
                .padding(padding)
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassIconButton(icon = Icons.Default.KeyboardArrowLeft) { navController.popBackStack() }
                Text(
                    text = "REPRODUCIENDO AHORA",
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                GlassIconButton(icon = Icons.Default.MoreVert) {}
            }

            Spacer(Modifier.height(32.dp))
            Box {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF5B21B6), Color(0xFF1E3A8A), Color(0xFF0F172A))))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(30.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = if (song.coverImage?.isEmpty() == true) R.drawable.ic_music_placeholder else song.coverImage
                        ),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color.Black.copy(alpha = 0.28f))
                )
            }

            Spacer(Modifier.height(28.dp))
            Text(song.title, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(song.artistName, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.titleMedium)
            Text(song.album.orEmpty(), color = Color.White.copy(alpha = 0.45f), style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(28.dp))
            Slider(
                value = currentPosition.coerceAtMost(duration).toFloat(),
                onValueChange = { exoPlayer?.seekTo(it.toLong()) },
                valueRange = 0f..duration.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF6D4AFF),
                    activeTrackColor = Color(0xFF6D4AFF),
                    inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                )
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatDuration(currentPosition), color = Color.White.copy(alpha = 0.7f))
                Text(formatDuration(duration), color = Color.White.copy(alpha = 0.7f))
            }

            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.toggleShuffleMode() }) {
                    Icon(Icons.Default.Shuffle, null, tint = Color.White.copy(alpha = 0.75f))
                }
                IconButton(onClick = { viewModel.toggleRepeatMode() }) {
                    Icon(Icons.Default.Repeat, null, tint = Color.White.copy(alpha = 0.75f))
                }
                IconButton(onClick = { viewModel.toggleFavoriteStatus() }) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        null,
                        tint = if (isFavorite) Color(0xFFFB7185) else Color.White.copy(alpha = 0.75f)
                    )
                }
                IconButton(onClick = { showEqDialog = true }) {
                    Icon(Icons.Default.Equalizer, null, tint = Color.White.copy(alpha = 0.85f))
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.playPrevious() }, enabled = canPlayPrevious) {
                    Icon(Icons.Default.SkipPrevious, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(34.dp))
                }

                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF4F46E5)))),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { if (isPlaying) viewModel.pause() else viewModel.resume() }) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                IconButton(onClick = { viewModel.playNext() }, enabled = canPlayNext) {
                    Icon(Icons.Default.SkipNext, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(34.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showEqDialog) {
        EqualizerDialog(equalizer = equalizer, onDismiss = { showEqDialog = false })
    }
}

@Composable
private fun GlassIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.65f))
        }
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
