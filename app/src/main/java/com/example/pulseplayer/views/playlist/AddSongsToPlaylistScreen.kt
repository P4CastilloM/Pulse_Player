package com.example.pulseplayer.views.playlist

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pulseplayer.R
import com.example.pulseplayer.data.PulsePlayerDatabase
import com.example.pulseplayer.data.entity.PlaylistSong
import com.example.pulseplayer.data.entity.Song
import com.example.pulseplayer.isLandscape
import com.example.pulseplayer.ui.components.MiniPlayerBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongsToPlaylistScreen(navController: NavController, playlistId: Int) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var allSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    val songDao = remember { PulsePlayerDatabase.getDatabase(context).songDao() }
    val playlistSongDao = remember { PulsePlayerDatabase.getDatabase(context).playlistSongDao() }

    val selectedSongs = remember { mutableStateListOf<Song>() }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            allSongs = songDao.getAll().first()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.playlist_add_song),
                        color = Color.White,
                        fontSize = 34.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF090B1A)),
            )
        },
        floatingActionButton = {
            val isFabEnabled = selectedSongs.isNotEmpty()

            val fabModifier = if (isLandscape()) Modifier.navigationBarsPadding() else Modifier
            FloatingActionButton(
                onClick = {
                    if (isFabEnabled) {
                        scope.launch(Dispatchers.IO) {
                            var currentOrder = 0
                            selectedSongs.forEach { song ->
                                playlistSongDao.insert(
                                    PlaylistSong(
                                        playlistId = playlistId,
                                        songId = song.idSong,
                                        songOrder = currentOrder++
                                    )
                                )
                            }
                            withContext(Dispatchers.Main) { navController.popBackStack() }
                        }
                    }
                },
                containerColor = Color(0xFF9C27B0),
                contentColor = Color.White,
                modifier = fabModifier,
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Añadir seleccionadas")
            }
        },
        bottomBar = {
            MiniPlayerBar(
                navController = navController,
                modifier = Modifier.fillMaxWidth().wrapContentHeight().navigationBarsPadding(),
            )
        },
        containerColor = Color(0xFF060911),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF060911))
        ) {
            if (allSongs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay canciones disponibles.",
                        color = Color.White.copy(alpha = 0.5f),
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(allSongs) { song ->
                        SelectableSongCardItem(
                            song = song,
                            isSelected = selectedSongs.contains(song),
                            onClick = {
                                if (selectedSongs.contains(song)) {
                                    selectedSongs.remove(song)
                                } else {
                                    selectedSongs.add(song)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SelectableSongCardItem(song: Song, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF9C27B0) else Color.White.copy(alpha = 0.07f),
        label = "borderColor"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFC968FF) else Color.White.copy(alpha = 0.5f),
        label = "iconColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .border(1.dp, borderColor, shape = RoundedCornerShape(18.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF121A31), Color(0xFF0A132B))
                    )
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val painter = rememberAsyncImagePainter(
                model = song.coverImage?.ifEmpty { R.drawable.ic_music_placeholder }
            )

            Image(
                painter = painter,
                contentDescription = "Cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                )
                Text(
                    text = song.artistName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.62f),
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seleccionado",
                    tint = iconColor,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 4.dp)
                )
            }

            Text(
                text = song.formattedDuration,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.88f),
            )
        }
    }
}
