package com.example.pulseplayer.views.playlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pulseplayer.PlaylistCreateScreen
import com.example.pulseplayer.PlaylistDetailScreen
import com.example.pulseplayer.R
import com.example.pulseplayer.data.entity.Playlist
import com.example.pulseplayer.isLandscape
import com.example.pulseplayer.ui.components.MiniPlayerBar
import com.example.pulseplayer.views.viewmodel.PlaylistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(navController: NavController) {
    val viewModel: PlaylistViewModel = viewModel()
    val playlists by viewModel.playlists.collectAsState()
    val landscape = isLandscape()

    // Estado para guardar la playlist seleccionada para eliminación
    var selectedPlaylistIdForDeletion by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.playlist_screen_title),
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                // Mostrar icono de eliminación en la TopAppBar si una playlist está seleccionada
                actions = {
                    selectedPlaylistIdForDeletion?.let {
                        IconButton(onClick = {
                            viewModel.deletePlaylist(it)
                            selectedPlaylistIdForDeletion = null // Deseleccionar después de eliminar
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar playlist", tint = Color.Red)
                        }
                    }
                }
            )
        },
        bottomBar = {
            MiniPlayerBar(
                navController = navController,
                modifier = Modifier.fillMaxWidth().wrapContentHeight().navigationBarsPadding(),
            )
        },
        floatingActionButton = {
            // Solo mostrar el FAB si no hay ninguna playlist seleccionada para eliminar
            if (selectedPlaylistIdForDeletion == null) {
                if (landscape) {
                    FloatingActionButton(
                        onClick = { navController.navigate(PlaylistCreateScreen) },
                        containerColor = Color(0xFF9C27B0),
                        contentColor = Color.White,
                        modifier = Modifier.navigationBarsPadding(),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Crear lista")
                    }
                } else {
                    FloatingActionButton(
                        onClick = { navController.navigate(PlaylistCreateScreen) },
                        containerColor = Color(0xFF9C27B0),
                        contentColor = Color.White,
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Crear lista")
                    }
                }
            }
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp) // Aplicar padding horizontal aquí
        ) {
            if (playlists.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.playlist_empty_message),
                        color = Color.LightGray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(vertical = 16.dp), // Aplicar padding vertical aquí
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(playlists) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            navController = navController,
                            isSelected = playlist.id.toLong() == selectedPlaylistIdForDeletion,
                            onLongClick = { selectedPlaylistIdForDeletion = playlist.id.toLong() },
                            onClick = {
                                if (selectedPlaylistIdForDeletion == null) {
                                    navController.navigate(PlaylistDetailScreen(playlistId = playlist.id))
                                } else {
                                    selectedPlaylistIdForDeletion = null // Deseleccionar si ya está en modo selección
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistCard(
    playlist: Playlist,
    navController: NavController,
    isSelected: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
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
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(16.dp)
            .then(
                if (isSelected) Modifier.background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                else Modifier
            ),
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