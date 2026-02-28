package com.example.pulseplayer.views.smartplaylists

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.pulseplayer.NowPlaying
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

private enum class SmartType(val label: String) {
    Weekly("Más escuchadas esta semana"),
    Recent("Descubiertas recientemente"),
    Silent30("No escuchadas en 30 días"),
    Night("Top nocturno"),
    Study("Modo estudio")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartPlaylistsScreen(navController: NavController, playerViewModel: PlayerViewModel) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf(SmartType.Weekly) }
    var tracks by remember { mutableStateOf<List<SmartPlaylistTrack>>(emptyList()) }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Playlists", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF090B1A))
            )
        },
        containerColor = Color(0xFF060911)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            LazyColumn(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(180.dp)
            ) {
                items(SmartType.entries) { type ->
                    val isSelected = selected == type
                    Text(
                        text = type.label,
                        color = if (isSelected) Color(0xFF8B5CF6) else Color.White.copy(alpha = 0.8f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selected = type }
                            .background(if (isSelected) Color(0xFF8B5CF6).copy(alpha = 0.15f) else Color.Transparent)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(tracks) { track ->
                    SmartTrackCard(track = track) {
                        scope.launch {
                            val song = withContext(Dispatchers.IO) {
                                PulsePlayerDatabase.getDatabase(context).songDao().getById(track.songId)
                            }
                            if (song != null) {
                                playerViewModel.playSong(song)
                                navController.navigate(NowPlaying(song.idSong, listOf(song.idSong)))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmartTrackCard(track: SmartPlaylistTrack, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
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
            Text(track.title, color = MaterialTheme.colorScheme.onSurface)
            Text(track.artistName, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("${track.playCount}", color = Color(0xFF8B5CF6))
    }
}

private fun daysAgo(days: Int): String {
    val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -days) }
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(calendar.time)
}
