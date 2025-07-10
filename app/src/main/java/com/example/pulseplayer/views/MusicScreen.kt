package com.example.pulseplayer.views

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
import com.example.pulseplayer.data.entity.Song
import com.example.pulseplayer.ui.components.MiniPlayerBar
import com.example.pulseplayer.views.viewmodel.PlayerViewModel
import com.example.pulseplayer.views.viewmodel.SongViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val songViewModel: SongViewModel = viewModel()
    val songs by songViewModel.allSongs.collectAsState()
    val playerViewModel: PlayerViewModel = viewModel()
    var selectedSongId by remember { mutableStateOf<Int?>(null) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_music),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
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
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(WindowInsets.systemBars.asPaddingValues())
        ) {
            items(songs) { song ->
                SongCardItem(
                    song = song,
                    isSelected = playerViewModel.currentSong.value?.idSong == song.idSong,
                    onClick = {
                        val allSongIds = songs.map { it.idSong } //navegar a la pantalla de reproducción
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = song.artistName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = song.formattedDuration,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}