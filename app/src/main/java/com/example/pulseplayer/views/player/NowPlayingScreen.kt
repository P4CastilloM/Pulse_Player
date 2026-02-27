package com.example.pulseplayer.views.player

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.core.graphics.drawable.toBitmap
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.pulseplayer.R
import com.example.pulseplayer.data.PulsePlayerDatabase
import com.example.pulseplayer.views.viewmodel.PlayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

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

    val glowColor by rememberDynamicGlowColor(song.coverImage)

    Scaffold(
        containerColor = Color(0xFF05060D),
        bottomBar = {
            Box(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.width(120.dp).height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.28f))
                )
            }
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF0A0A14), Color(0xFF090B1A), Color(0xFF06070F))))
                .padding(padding)
                .padding(horizontal = 18.dp)
        ) {
            val isLandscape = maxWidth > maxHeight

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                HeaderRow(navController = navController)
                if (isLandscape) {
                    LandscapePlayerContent(
                        song = song,
                        glowColor = glowColor,
                        currentPosition = currentPosition,
                        duration = duration,
                        isFavorite = isFavorite,
                        isPlaying = isPlaying,
                        canPlayPrevious = canPlayPrevious,
                        canPlayNext = canPlayNext,
                        onSeek = { exoPlayer?.seekTo(it.toLong()) },
                        onShuffle = viewModel::toggleShuffleMode,
                        onRepeat = viewModel::toggleRepeatMode,
                        onFavorite = viewModel::toggleFavoriteStatus,
                        onEq = { showEqDialog = true },
                        onPrev = viewModel::playPrevious,
                        onPlayPause = { if (isPlaying) viewModel.pause() else viewModel.resume() },
                        onNext = viewModel::playNext,
                    )
                } else {
                    PortraitPlayerContent(
                        song = song,
                        glowColor = glowColor,
                        currentPosition = currentPosition,
                        duration = duration,
                        isFavorite = isFavorite,
                        isPlaying = isPlaying,
                        canPlayPrevious = canPlayPrevious,
                        canPlayNext = canPlayNext,
                        onSeek = { exoPlayer?.seekTo(it.toLong()) },
                        onShuffle = viewModel::toggleShuffleMode,
                        onRepeat = viewModel::toggleRepeatMode,
                        onFavorite = viewModel::toggleFavoriteStatus,
                        onEq = { showEqDialog = true },
                        onPrev = viewModel::playPrevious,
                        onPlayPause = { if (isPlaying) viewModel.pause() else viewModel.resume() },
                        onNext = viewModel::playNext,
                    )
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
private fun HeaderRow(navController: NavController) {
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
}

@Composable
private fun PortraitPlayerContent(
    song: com.example.pulseplayer.data.entity.Song,
    glowColor: Color,
    currentPosition: Long,
    duration: Long,
    isFavorite: Boolean,
    isPlaying: Boolean,
    canPlayPrevious: Boolean,
    canPlayNext: Boolean,
    onSeek: (Float) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onFavorite: () -> Unit,
    onEq: () -> Unit,
    onPrev: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    Spacer(Modifier.height(26.dp))
    AlbumArtWithGlow(song.coverImage, glowColor, size = 250.dp)
    Spacer(Modifier.height(28.dp))
    SongTexts(song)
    Spacer(Modifier.height(28.dp))
    PlayerProgress(currentPosition, duration, onSeek)
    Spacer(Modifier.height(18.dp))
    ModeButtons(isFavorite, onShuffle, onRepeat, onFavorite, onEq)
    Spacer(Modifier.height(12.dp))
    MainTransportButtons(isPlaying, canPlayPrevious, canPlayNext, onPrev, onPlayPause, onNext)
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun LandscapePlayerContent(
    song: com.example.pulseplayer.data.entity.Song,
    glowColor: Color,
    currentPosition: Long,
    duration: Long,
    isFavorite: Boolean,
    isPlaying: Boolean,
    canPlayPrevious: Boolean,
    canPlayNext: Boolean,
    onSeek: (Float) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onFavorite: () -> Unit,
    onEq: () -> Unit,
    onPrev: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        AlbumArtWithGlow(song.coverImage, glowColor, size = 210.dp)
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            SongTexts(song)
            Spacer(Modifier.height(18.dp))
            PlayerProgress(currentPosition, duration, onSeek)
            Spacer(Modifier.height(16.dp))
            ModeButtons(isFavorite, onShuffle, onRepeat, onFavorite, onEq)
            Spacer(Modifier.height(8.dp))
            MainTransportButtons(isPlaying, canPlayPrevious, canPlayNext, onPrev, onPlayPause, onNext)
        }
    }
}

@Composable
private fun AlbumArtWithGlow(coverImage: String?, glowColor: Color, size: androidx.compose.ui.unit.Dp) {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(size + 120.dp)
                .blur(72.dp)
                .background(glowColor.copy(alpha = 0.34f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(size + 64.dp)
                .blur(40.dp)
                .background(glowColor.copy(alpha = 0.42f), RoundedCornerShape(44.dp))
        )

        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(30.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF5B21B6), Color(0xFF1E3A8A), Color(0xFF0F172A))))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(30.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = if (coverImage?.isEmpty() == true) R.drawable.ic_music_placeholder else coverImage),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.18f)))
        }
    }
}
@Composable
private fun SongTexts(song: com.example.pulseplayer.data.entity.Song) {
    Text(song.title, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    Text(song.artistName, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.titleMedium)
    Text(song.album.orEmpty(), color = Color.White.copy(alpha = 0.45f), style = MaterialTheme.typography.bodyMedium)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerProgress(currentPosition: Long, duration: Long, onSeek: (Float) -> Unit) {
    Slider(
        value = currentPosition.coerceAtMost(duration).toFloat(),
        onValueChange = onSeek,
        valueRange = 0f..duration.toFloat(),
        thumb = {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6D4AFF))
            )
        },
        colors = SliderDefaults.colors(
            thumbColor = Color(0xFF6D4AFF),
            activeTrackColor = Color.White.copy(alpha = 0.22f),
            inactiveTrackColor = Color.White.copy(alpha = 0.22f)
        ),
        modifier = Modifier.height(14.dp)
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(formatDuration(currentPosition), color = Color(0xFFA8B0C8), style = MaterialTheme.typography.labelMedium)
        Text(formatDuration(duration), color = Color(0xFFA8B0C8), style = MaterialTheme.typography.labelMedium)
    }
}
@Composable
private fun ModeButtons(isFavorite: Boolean, onShuffle: () -> Unit, onRepeat: () -> Unit, onFavorite: () -> Unit, onEq: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onShuffle) { Icon(Icons.Default.Shuffle, null, tint = Color.White.copy(alpha = 0.75f)) }
        IconButton(onClick = onRepeat) { Icon(Icons.Default.Repeat, null, tint = Color.White.copy(alpha = 0.75f)) }
        IconButton(onClick = onFavorite) {
            Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = if (isFavorite) Color(0xFFFB7185) else Color.White.copy(alpha = 0.75f))
        }
        IconButton(onClick = onEq) { Icon(Icons.Default.Equalizer, null, tint = Color.White.copy(alpha = 0.85f)) }
    }
}

@Composable
private fun MainTransportButtons(
    isPlaying: Boolean,
    canPlayPrevious: Boolean,
    canPlayNext: Boolean,
    onPrev: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev, enabled = canPlayPrevious) {
            Icon(Icons.Default.SkipPrevious, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(34.dp))
        }

        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF4F46E5)))),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onPlayPause) {
                Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(36.dp))
            }
        }

        IconButton(onClick = onNext, enabled = canPlayNext) {
            Icon(Icons.Default.SkipNext, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(34.dp))
        }
    }
}

@Composable
private fun rememberDynamicGlowColor(coverImage: String?): androidx.compose.runtime.State<Color> {
    val context = LocalContext.current
    return produceState(initialValue = Color(0xFF6D4AFF), coverImage) {
        value = extractDominantGlowColor(context.imageLoader, context, coverImage) ?: Color(0xFF6D4AFF)
    }
}

private suspend fun extractDominantGlowColor(
    imageLoader: coil.ImageLoader,
    context: android.content.Context,
    coverImage: String?
): Color? {
    if (coverImage.isNullOrBlank()) return null
    return withContext(Dispatchers.IO) {
        runCatching {
            val request = ImageRequest.Builder(context)
                .data(coverImage)
                .size(64, 64)
                .allowHardware(false)
                .build()
            val result = imageLoader.execute(request) as? SuccessResult ?: return@withContext null
            val bitmap = result.drawable.toBitmap(64, 64, Bitmap.Config.ARGB_8888)
            dominantColorFromBitmap(bitmap)
        }.getOrNull()
    }
}

private fun dominantColorFromBitmap(bitmap: Bitmap): Color {
    var r = 0f
    var g = 0f
    var b = 0f
    var weightSum = 0f

    val widthStep = (bitmap.width / 20).coerceAtLeast(1)
    val heightStep = (bitmap.height / 20).coerceAtLeast(1)

    for (x in 0 until bitmap.width step widthStep) {
        for (y in 0 until bitmap.height step heightStep) {
            val pixel = bitmap.getPixel(x, y)
            val red = AndroidColor.red(pixel)
            val green = AndroidColor.green(pixel)
            val blue = AndroidColor.blue(pixel)

            val maxChannel = maxOf(red, green, blue).toFloat()
            val minChannel = minOf(red, green, blue).toFloat()
            val saturation = if (maxChannel == 0f) 0f else (maxChannel - minChannel) / maxChannel
            val weight = 0.35f + saturation

            r += red * weight
            g += green * weight
            b += blue * weight
            weightSum += weight
        }
    }

    if (weightSum == 0f) return Color(0xFF6D4AFF)

    val rawColor = Color((r / weightSum) / 255f, (g / weightSum) / 255f, (b / weightSum) / 255f, 1f)
    val boosted = rawColor.copy(alpha = 1f)
    return if (boosted.luminance() < 0.15f) {
        boosted.copy(
            red = (boosted.red + 0.20f).coerceAtMost(1f),
            blue = (boosted.blue + 0.20f).coerceAtMost(1f)
        )
    } else {
        boosted
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
        IconButton(onClick = onClick) { Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.65f)) }
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
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%02d:%02d", min, sec)
}
