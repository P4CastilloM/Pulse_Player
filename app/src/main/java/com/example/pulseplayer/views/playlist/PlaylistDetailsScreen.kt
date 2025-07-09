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
import androidx.compose.material.icons.filled.Add // Necesitamos este icono
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pulseplayer.Music
import com.example.pulseplayer.NowPlaying
import com.example.pulseplayer.R
import com.example.pulseplayer.data.PulsePlayerDatabase
import com.example.pulseplayer.data.entity.Playlist
import com.example.pulseplayer.data.entity.Song
import com.example.pulseplayer.ui.components.MiniPlayerBar
import com.example.pulseplayer.views.viewmodel.PlayerViewModel
import com.example.pulseplayer.views.viewmodel.PlaylistViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Importa la nueva ruta de navegación
import com.example.pulseplayer.AddSongsToPlaylistScreen // <-- ¡Importa tu nueva ruta!


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailsScreen(navController: NavController, playlistId: Int) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val playlistViewModel: PlaylistViewModel = viewModel()
    val playerViewModel: PlayerViewModel = viewModel()

    var playlist by remember { mutableStateOf<Playlist?>(null) }

    val playlistSongDao = remember { PulsePlayerDatabase.getDatabase(context).playlistSongDao() }

    val songs by remember(playlistId) {
        playlistSongDao.getSongsInPlaylist(playlistId)
    }.collectAsState(initial = emptyList())

    LaunchedEffect(playlistId) {
        scope.launch(Dispatchers.IO) {
            playlist = playlistViewModel.getPlaylistById(playlistId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = playlist?.name ?: "Detalles de la Lista",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        // ¡Añade el FloatingActionButton aquí!
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Navega a la pantalla para añadir canciones, pasando el ID de la playlist actual
                        navController.navigate(AddSongsToPlaylistScreen(playlistId = playlistId))
                },
                containerColor = Color(0xFF9C27B0), // Color morado similar a tu otro FAB
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir canciones")
            }
        },
        bottomBar = {
            MiniPlayerBar(
                navController = navController,
                modifier = Modifier.fillMaxWidth().wrapContentHeight().navigationBarsPadding(),
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            if (songs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.playlist_empty_message2),
                        color = Color.LightGray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(songs) { song ->
                        SongCardItem(
                            song = song,
                            isSelected = playerViewModel.currentSong.value?.idSong == song.idSong,
                            onClick = {
                                val allSongIds = songs.map { it.idSong } // Esto ya es List<Int>
                                val startIndex = songs.indexOfFirst { it.idSong == song.idSong }
                                playerViewModel.playPlaylist(songs, startIndex)
                                navController.navigate(NowPlaying(song.idSong, allSongIds)) { // <--- ¡CAMBIO AQUÍ! Se pasa directamente allSongIds que ya es List<Int>
                                    popUpTo(Music) { inclusive = false }
                                    launchSingleTop = true
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
fun SongCardItem(song: Song, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF9C27B0) else Color.Transparent,
        label = "borderColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .border(2.dp, borderColor, shape = RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                Text(song.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(song.artistName, color = Color.LightGray, fontSize = 14.sp)
            }

            Text(song.formattedDuration, color = Color.White, fontSize = 14.sp)
        }
    }
}

