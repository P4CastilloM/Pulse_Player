package com.example.pulseplayer.views.playbackhistory

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.pulseplayer.NowPlaying
import com.example.pulseplayer.R
import com.example.pulseplayer.data.PulsePlayerDatabase
import com.example.pulseplayer.data.entity.PlaybackHistory
import com.example.pulseplayer.data.entity.Song
import com.example.pulseplayer.ui.components.DeleteConfirmationDialog
import com.example.pulseplayer.ui.components.MiniPlayerBar
import com.example.pulseplayer.views.viewmodel.PlayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackHistoryScreen(navController: NavController, playerViewModel: PlayerViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showConfirmDialog by remember { mutableStateOf(false) }

    var selectedSongId by remember { mutableStateOf<Int?>(null) }
    var historySongs by remember { mutableStateOf<List<Pair<PlaybackHistory, Song>>>(emptyList()) }

    // Cargar historial al iniciar
    LaunchedEffect(true) {
        withContext(Dispatchers.IO) {
            val db = PulsePlayerDatabase.getDatabase(context)
            val allHistory = db.playbackHistoryDao().getAllOnce()
            val songDao = db.songDao()

            val historyWithSongs = allHistory.mapNotNull { history ->
                val song = songDao.getById(history.songId)
                song?.let { history to it }
            }

            historySongs = historyWithSongs
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    "Historial",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    ) },
                actions = {
                    IconButton(onClick = { showConfirmDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar historial", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
            )
        },
        bottomBar = {
            MiniPlayerBar(
                navController = navController,
                modifier = Modifier.fillMaxWidth().wrapContentHeight().navigationBarsPadding(),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            items(historySongs) { (entry, song) ->
                HistorySongCard(
                    song = song,
                    playedAt = entry.playedAt,
                    isSelected = selectedSongId == song.idSong,
                    onClick = {
                        selectedSongId = song.idSong
                        playerViewModel.playSong(song)
                        navController.navigate(NowPlaying(song.idSong, listOf(song.idSong)))
                    }
                )
            }
        }
    }
    if (showConfirmDialog) {
        DeleteConfirmationDialog(
            title = "Eliminar historial",
            message = "¿Estás seguro de que quieres borrar todo el historial de reproducción?",
            onConfirm = {
                showConfirmDialog = false
                scope.launch {
                    withContext(Dispatchers.IO) {
                        val db = PulsePlayerDatabase.getDatabase(context)
                        db.playbackHistoryDao().deleteAll()
                        historySongs = emptyList()
                    }
                }
            },
            onDismiss = { showConfirmDialog = false }
        )
    }

}

@Composable
fun HistorySongCard(
    song: Song,
    playedAt: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF9C27B0) else Color.Transparent
    val borderWidth = if (isSelected) 2.dp else 0.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val imageModifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(12.dp))

        if (!song.coverImage.isNullOrBlank()) {
            AsyncImage(
                model = song.coverImage,
                contentDescription = null,
                modifier = imageModifier
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_music_placeholder),
                contentDescription = null,
                modifier = imageModifier
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = formatDateShort(playedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

fun formatDateShort(dateTimeString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(dateTimeString)
        val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        outputFormat.format(date!!)
    } catch (e: Exception) {
        dateTimeString
    }
}
