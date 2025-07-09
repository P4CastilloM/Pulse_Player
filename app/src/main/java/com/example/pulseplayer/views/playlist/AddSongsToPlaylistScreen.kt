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
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Usar imageVector
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        floatingActionButton = {
            val isFabEnabled = selectedSongs.isNotEmpty()

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
                //enabled = isFabEnabled // Asegúrate de que esta propiedad exista en tu Material3
            ) {
                Icon(
                    imageVector = Icons.Default.Check, // Usar imageVector
                    contentDescription = "Añadir seleccionadas"
                )
            }
        }, // <<<<<<<<<<<<<< CIERRE DE LA LAMBDA floatingActionButton DEL SCAFFOLD >>>>>>>>>>>>>>>
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            if (allSongs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay canciones disponibles.",
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
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
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

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check, // Usar imageVector
                    contentDescription = "Seleccionado",
                    tint = Color(0xFF9C27B0),
                    modifier = Modifier.size(24.dp).padding(end = 4.dp)
                )
            }

            Text(song.formattedDuration, color = Color.White, fontSize = 14.sp)
        }
    }
}