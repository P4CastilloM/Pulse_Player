package com.example.pulseplayer.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pulseplayer.NowPlaying
import com.example.pulseplayer.ui.components.MiniPlayerBar
import com.example.pulseplayer.views.viewmodel.PlayerViewModel
import com.example.pulseplayer.views.viewmodel.SongViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(navController: NavController) {
    val songViewModel: SongViewModel = viewModel()
    val playerViewModel: PlayerViewModel = viewModel()
    val songs by songViewModel.allSongs.collectAsState()

    val albums = songs
        .groupBy { it.album?.trim().takeUnless { it.isNullOrEmpty() } ?: "Sin álbum" }
        .map { (album, albumSongs) -> album to albumSongs.sortedBy { it.trackNumber?.toIntOrNull() ?: Int.MAX_VALUE } }
        .sortedBy { it.first.lowercase() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Álbumes", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF090B1A))
            )
        },
        bottomBar = {
            MiniPlayerBar(navController = navController, modifier = Modifier.fillMaxWidth())
        },
        containerColor = Color(0xFF060911)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF060911))
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(albums) { (album, albumSongs) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (albumSongs.isNotEmpty()) {
                                playerViewModel.playPlaylist(albumSongs, 0)
                                navController.navigate(NowPlaying(albumSongs.first().idSong, albumSongs.map { it.idSong }))
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.horizontalGradient(listOf(Color(0xFF172554), Color(0xFF1E1B4B))))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Album, contentDescription = null, tint = Color(0xFFA78BFA))
                        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                            Text(album, color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Text("${albumSongs.size} canciones", color = Color.White.copy(alpha = 0.65f))
                        }
                        Text(
                            text = albumSongs.firstOrNull()?.artistName ?: "",
                            color = Color.White.copy(alpha = 0.55f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
