package com.example.pulseplayer.views.favorite

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pulseplayer.NowPlaying
import com.example.pulseplayer.data.entity.Song
import com.example.pulseplayer.ui.components.MiniPlayerBar
import com.example.pulseplayer.views.viewmodel.PlayerViewModel
import com.example.pulseplayer.views.viewmodel.SongViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    navController: NavController,
    songViewModel: SongViewModel = viewModel(),
    playerViewModel: PlayerViewModel = viewModel()
) {
    val favorites by songViewModel.favoriteSongs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Favoritos",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        bottomBar = {
            MiniPlayerBar(
                navController = navController,
                modifier = Modifier.fillMaxWidth().wrapContentHeight().navigationBarsPadding(),
            )
        },
        containerColor = Color.Black
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (favorites.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Presione el botón ❤️ para agregar canciones a favoritos",
                            color = Color.LightGray,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                items(favorites) { song ->
                    SongCardItem(
                        song = song,
                        isSelected = playerViewModel.currentSong.value?.idSong == song.idSong,
                        onClick = {
                            val allSongIds = favorites.map { it.idSong }
                            val startIndex = favorites.indexOfFirst { it.idSong == song.idSong }
                            playerViewModel.playPlaylist(favorites, startIndex)
                            navController.navigate(NowPlaying(song.idSong, allSongIds)) {
                                popUpTo("menu") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
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
                model = song.coverImage?.ifEmpty { null },
                fallback = painterResource(id = com.example.pulseplayer.R.drawable.ic_music_placeholder)
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
