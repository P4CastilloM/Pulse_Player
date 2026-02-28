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
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pulseplayer.AddSongsToPlaylistScreen
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
                        text = playlist?.name ?: stringResource(R.string.playlist_screen_title),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold
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
            FloatingActionButton(
                onClick = { navController.navigate(AddSongsToPlaylistScreen(playlistId = playlistId)) },
                containerColor = Color(0xFF9C27B0),
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
        containerColor = Color(0xFF060911),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF060911))
        ) {
            if (songs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.playlist_empty_message2),
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(songs) { song ->
                        SongCardItem(
                            song = song,
                            isSelected = playerViewModel.currentSong.value?.idSong == song.idSong,
                            onClick = {
                                val allSongIds = songs.map { it.idSong }
                                val startIndex = songs.indexOfFirst { it.idSong == song.idSong }
                                playerViewModel.playPlaylist(songs, startIndex)
                                navController.navigate(NowPlaying(song.idSong, allSongIds)) {
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
        targetValue = if (isSelected) Color(0xFF9C27B0) else Color.White.copy(alpha = 0.06f),
        label = "borderColor"
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
                Text(song.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(song.artistName, color = Color.White.copy(alpha = 0.58f), fontSize = 14.sp)
            }

            Text(song.formattedDuration, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
        }
    }
}
