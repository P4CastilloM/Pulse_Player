package com.example.pulseplayer.views.smartplaylists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.pulseplayer.R
import com.example.pulseplayer.data.PulsePlayerDatabase
import com.example.pulseplayer.data.entity.SmartPlaylistTrack
import com.example.pulseplayer.views.viewmodel.PlayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

private enum class SmartType(
    val label: String,
    val emoji: String,
    val description: String
) {
    Weekly("Más escuchadas esta semana", "🔥", "Basado en tus reproducciones de los últimos 7 días"),
    Recent("Descubiertas recientemente", "✨", "Canciones escuchadas por primera vez en periodo reciente"),
    Silent30("No escuchadas en 30 días", "💤", "Redescubre joyas olvidadas de tu biblioteca"),
    Night("Top nocturno", "🌙", "Lo que más suena entre 22:00 y 06:00"),
    Study("Modo estudio", "📚", "Selección suave para concentración")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmartPlaylistsScreen(navController: NavController, playerViewModel: PlayerViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selected by remember { mutableStateOf(SmartType.Weekly) }
    var tracks by remember { mutableStateOf<List<SmartPlaylistTrack>>(emptyList()) }
    var expandedInfo by remember { mutableStateOf(false) }
    val likedSongs = remember { mutableStateMapOf<Int, Boolean>() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selected) {
        tracks = withContext(Dispatchers.IO) {
            val dao = PulsePlayerDatabase.getDatabase(context).playbackHistoryDao()
            when (selected) {
                SmartType.Weekly -> dao.getMostPlayedSince(daysAgo(7))
                SmartType.Recent -> dao.getRecentlyDiscovered(daysAgo(10))
                SmartType.Silent30 -> dao.getNotPlayedSince(daysAgo(30))
                SmartType.Night -> dao.getTopNightTracks()
                SmartType.Study -> dao.getStudyModeTracks()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(Color(0xFF0A0A0F), Color(0xFF111827), Color(0xFF080810))))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                HeaderSection()
            }

            item {
                Text("PLAYLISTS INTELIGENTES", color = Color.White.copy(alpha = 0.45f), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.padding(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmartType.entries.forEach { type ->
                        val active = type == selected
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (active) Brush.horizontalGradient(listOf(Color(0x337C3AED), Color(0x333B82F6)))
                                    else Brush.horizontalGradient(listOf(Color(0x12FFFFFF), Color(0x0BFFFFFF)))
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (active) Color(0xAA7C3AED) else Color.White.copy(alpha = 0.09f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { selected = type }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(type.emoji)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(type.label, color = Color.White.copy(alpha = if (active) 0.95f else 0.74f), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            item {
                SummaryCard(
                    selected = selected,
                    tracks = tracks,
                    onPlay = {
                        scope.launch {
                            val songs = withContext(Dispatchers.IO) {
                                val songDao = PulsePlayerDatabase.getDatabase(context).songDao()
                                tracks.mapNotNull { songDao.getById(it.songId) }
                            }
                            if (songs.isNotEmpty()) playerViewModel.playPlaylist(songs, 0)
                        }
                    },
                    onShuffle = {
                        scope.launch {
                            val songs = withContext(Dispatchers.IO) {
                                val songDao = PulsePlayerDatabase.getDatabase(context).songDao()
                                tracks.mapNotNull { songDao.getById(it.songId) }
                            }
                            if (songs.isNotEmpty()) {
                                val start = Random.nextInt(songs.size)
                                playerViewModel.playPlaylist(songs.shuffled(), start.coerceAtMost(songs.lastIndex))
                            }
                        }
                    }
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("CANCIONES", color = Color.White.copy(alpha = 0.45f), fontWeight = FontWeight.SemiBold)
                    Text("${tracks.size} TRACKS", color = Color(0xFFB794F6), fontWeight = FontWeight.Bold)
                }
            }

            if (tracks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Selecciona una playlist", color = Color.White.copy(alpha = 0.6f))
                    }
                }
            } else {
                item {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                    ) {
                        tracks.forEach { track ->
                            PlaylistSongItem(
                                track = track,
                                liked = likedSongs[track.songId] == true,
                                onLike = { likedSongs[track.songId] = !(likedSongs[track.songId] ?: false) }
                            )
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(1.dp, Color.White.copy(alpha = 0.09f), RoundedCornerShape(24.dp))
                        .clickable { expandedInfo = !expandedInfo }
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x337C3AED)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("ℹ️")
                            }
                            Spacer(Modifier.size(10.dp))
                            Text("¿Cómo se generan?", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                        AnimatedVisibility(
                            visible = expandedInfo,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = "Estas playlists se calculan localmente con SQLite usando historial, fechas y frecuencia. Todo se procesa en tu dispositivo.",
                                color = Color.White.copy(alpha = 0.72f),
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text("Smart Playlists", color = Color.White, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Automáticas • 100% local", color = Color.White.copy(alpha = 0.62f))
                Spacer(Modifier.size(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0x3322C55E))
                        .border(1.dp, Color(0x6622C55E), RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("SQLite", color = Color(0xFF4ADE80), fontSize = 11.sp)
                }
            }
        }

        IconButton(
            onClick = {},
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.09f), RoundedCornerShape(16.dp))
        ) {
            Icon(Icons.Outlined.Settings, contentDescription = null, tint = Color.White.copy(alpha = 0.64f))
        }
    }
}

@Composable
private fun SummaryCard(
    selected: SmartType,
    tracks: List<SmartPlaylistTrack>,
    onPlay: () -> Unit,
    onShuffle: () -> Unit
) {
    val durationMinutes = tracks.sumOf { it.durationMs } / 60000
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0x1AFFFFFF), Color(0x0DFFFFFF))))
            .border(1.dp, Color.White.copy(alpha = 0.09f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(selected.emoji)
                Spacer(Modifier.size(8.dp))
                Text(selected.label, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Text(selected.description, color = Color.White.copy(alpha = 0.65f))
            Row {
                Text("🎵 ${tracks.size} canciones", color = Color(0xFFC4B5FD), fontSize = 12.sp)
                Spacer(Modifier.size(14.dp))
                Text("🕒 ${durationMinutes} min", color = Color(0xFF93C5FD), fontSize = 12.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onPlay,
                    enabled = tracks.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .weight(1f)
                        .background(Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6))), RoundedCornerShape(16.dp))
                ) {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.size(6.dp))
                    Text("Reproducir", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onShuffle,
                    enabled = tracks.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x14FFFFFF)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.Shuffle, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
                    Spacer(Modifier.size(6.dp))
                    Text("Mezclar", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PlaylistSongItem(track: SmartPlaylistTrack, liked: Boolean, onLike: () -> Unit) {
    val playBadgeRatio by animateFloatAsState(
        targetValue = (track.playCount / 50f).coerceIn(0.2f, 1f),
        animationSpec = tween(500),
        label = "badge"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!track.coverImage.isNullOrBlank()) {
            AsyncImage(model = track.coverImage, contentDescription = null, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)))
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(Color(0x337C3AED), Color(0x223B82F6)))),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_music_placeholder),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.size(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(track.title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(track.artistName, color = Color.White.copy(alpha = 0.5f))
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x225B21B6))
                .border(1.dp, Color(0x447C3AED), RoundedCornerShape(10.dp))
                .padding(horizontal = (8 * playBadgeRatio).dp, vertical = 4.dp)
        ) {
            Text("${track.playCount}×", color = Color(0xFFC4B5FD), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        IconButton(onClick = onLike) {
            Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, tint = if (liked) Color(0xFFF43F5E) else Color.White.copy(alpha = 0.4f))
        }
        Icon(Icons.Outlined.MoreVert, contentDescription = null, tint = Color.White.copy(alpha = 0.34f))
    }
}

@Composable
fun SmartTrackCard(track: SmartPlaylistTrack, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!track.coverImage.isNullOrBlank()) {
            AsyncImage(model = track.coverImage, contentDescription = null, modifier = Modifier.size(56.dp))
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_music_placeholder),
                contentDescription = null,
                modifier = Modifier.size(56.dp)
            )
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(track.title, color = Color.White)
            Text(track.artistName, color = Color.White.copy(alpha = 0.6f))
        }
        Text("${track.playCount}", color = Color(0xFF8B5CF6))
    }
}

private fun daysAgo(days: Int): String {
    val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -days) }
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(calendar.time)
}
