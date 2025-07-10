package com.example.pulseplayer.views.playlist // Asegúrate de que este sea el paquete correcto

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.* // Importar todo de Material3 para asegurar acceso
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
import com.example.pulseplayer.R
import com.example.pulseplayer.data.PulsePlayerDatabase
import com.example.pulseplayer.data.entity.PlaylistSong
import com.example.pulseplayer.data.entity.Song
import com.example.pulseplayer.isLandscape
import com.example.pulseplayer.ui.components.MiniPlayerBar
import com.example.pulseplayer.views.viewmodel.PlayerViewModel // Puede que necesites este si MiniPlayerBar lo usa
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongsToPlaylistScreen(navController: NavController, playlistId: Int) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var allSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    val songDao = remember { PulsePlayerDatabase.getDatabase(context).songDao() }
    val playlistSongDao = remember { PulsePlayerDatabase.getDatabase(context).playlistSongDao() }

    val selectedSongs = remember { mutableStateListOf<Song>() } // Usamos mutableStateListOf para observables de lista

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            allSongs = songDao.getAll().first() // Obtener todas las canciones una sola vez
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.playlist_add_song),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
            )
        },
        floatingActionButton = {
            val isFabEnabled = selectedSongs.isNotEmpty()

            if (isLandscape()) {
                FloatingActionButton(
                    onClick = {
                        if (isFabEnabled) {
                            scope.launch(Dispatchers.IO) { // Ejecuta la inserción en el hilo de fondo
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
                                // Después de la operación de base de datos, cambia al hilo principal para navegar
                                withContext(Dispatchers.Main) {
                                    navController.popBackStack() // <<<<<<<<<<<<<< AHORA EN EL HILO PRINCIPAL
                                }
                            }
                        }
                    },
                    containerColor = Color(0xFF9C27B0),
                    contentColor = Color.White,
                    modifier = Modifier.navigationBarsPadding(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Check, // Usar imageVector
                        contentDescription = "Añadir seleccionadas"
                    )
                }
            } else {
                FloatingActionButton(
                    onClick = {
                        if (isFabEnabled) {
                            scope.launch(Dispatchers.IO) { // Ejecuta la inserción en el hilo de fondo
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
                                // Después de la operación de base de datos, cambia al hilo principal para navegar
                                withContext(Dispatchers.Main) {
                                    navController.popBackStack() // <<<<<<<<<<<<<< AHORA EN EL HILO PRINCIPAL
                                }
                            }
                        }
                    },
                    containerColor = Color(0xFF9C27B0),
                    contentColor = Color.White,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check, // Usar imageVector
                        contentDescription = "Añadir seleccionadas"
                    )
                }
            }
        },
        bottomBar = {
            MiniPlayerBar(
                navController = navController,
                modifier = Modifier.fillMaxWidth().wrapContentHeight().navigationBarsPadding(),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (allSongs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay canciones disponibles.",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
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
        targetValue = if (isSelected) Color(0xFF9C27B0) else Color.Transparent,
        label = "borderColor"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF3A004C) else Color(0xFF1C1C1E),
        label = "backgroundColor"
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
                    text = song.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check, // Usar imageVector
                    contentDescription = "Seleccionado",
                    tint = Color(0xFF9C27B0),
                    modifier = Modifier.size(24.dp).padding(end = 4.dp)
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