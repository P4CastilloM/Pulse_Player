package com.example.pulseplayer.views.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pulseplayer.PlaylistCreateScreen
import com.example.pulseplayer.PlaylistDetailScreen
import com.example.pulseplayer.data.entity.Playlist
import com.example.pulseplayer.ui.components.MiniPlayerBar
import com.example.pulseplayer.views.viewmodel.PlaylistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(navController: NavController) {
    val viewModel: PlaylistViewModel = viewModel()
    val playlists by viewModel.playlists.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Listas de Reproducción",
                        fontSize = 20.sp,
                        color = Color.White
                    )
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(PlaylistCreateScreen) },
                containerColor = Color(0xFF9C27B0),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear lista")
            }
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (playlists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Presione el botón + para crear una lista de reproducción",
                        color = Color.LightGray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(playlists) { playlist ->
                        PlaylistCard(playlist,navController)
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistCard(playlist: Playlist, navController: NavController) {
    val colors = listOf(
        listOf(Color(0xFFFF416C), Color(0xFFFF4B2B)),
        listOf(Color(0xFF2193b0), Color(0xFF6dd5ed)),
        listOf(Color(0xFF7F00FF), Color(0xFFE100FF)),
        listOf(Color(0xFFff6a00), Color(0xFFee0979)),
        listOf(Color(0xFF833ab4), Color(0xFFfd1d1d), Color(0xFFfcb045))
    )
    val gradient = remember { colors.random() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(gradient)
            )
            .clickable { navController.navigate(PlaylistDetailScreen(playlistId = playlist.id)) }
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = playlist.name,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}