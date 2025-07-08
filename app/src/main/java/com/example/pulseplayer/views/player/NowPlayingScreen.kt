package com.example.pulseplayer.views.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pulseplayer.R
import com.example.pulseplayer.data.PulsePlayerDatabase
import com.example.pulseplayer.data.entity.Song
import com.example.pulseplayer.views.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(navController: NavController, songId: Int, songIds: List<Int>, viewModel: PlayerViewModel = viewModel()) {
    val context = LocalContext.current
    val currentSong by viewModel.currentSong // Observa la canción actual del ViewModel

    val exoPlayer = viewModel.getPlayer() // Obtiene la instancia del reproductor de ExoPlayerManager
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }

    // Estado para habilitar/deshabilitar los botones de navegación
    var canPlayNext by remember { mutableStateOf(false) }
    var canPlayPrevious by remember { mutableStateOf(false) }

    // Estados visuales (solo estético)
    var isShuffleEnabled by remember { mutableStateOf(false) }
    var isRepeatEnabled by remember { mutableStateOf(false) }
    val isFavorite by viewModel.isCurrentFavorite

    LaunchedEffect(currentSong?.idSong) {
        viewModel.checkIfCurrentSongIsFavorite()
    }

    // Actualiza el progreso de la reproducción y la duración
    LaunchedEffect(exoPlayer) {
        // Asegúrate de que el exoPlayer no sea nulo y esté listo para obtener la posición/duración
        exoPlayer?.let { player ->
            while (true) {
                currentPosition = player.currentPosition
                // Asegura que la duración no sea cero para evitar divisiones por cero en el Slider
                duration = player.duration.takeIf { d -> d > 0 } ?: 1L

                // Actualiza el estado de los botones
                canPlayNext = player.hasNextMediaItem()
                canPlayPrevious = player.hasPreviousMediaItem()

                delay(500) // Actualiza cada 500ms
            }
        }
    }

    // Carga la canción o playlist cuando la pantalla se lanza o los IDs cambian
    LaunchedEffect(songId, songIds) {
        // Solo carga la playlist si la canción actual del ViewModel no coincide con la solicitada,
        // o si no hay ninguna canción reproduciéndose actualmente.
        // Esto evita recargar innecesariamente la misma canción/playlist.
        if (viewModel.currentSong.value?.idSong != songId) {
            val dao = PulsePlayerDatabase.getDatabase(context).songDao()
            // Mapea los IDs a objetos Song, filtrando cualquier resultado nulo
            val songList = songIds.mapNotNull { dao.getById(it) }

            val startIndex = songList.indexOfFirst { it.idSong == songId }

            if (startIndex != -1) {
                // Llama a playPlaylist del ViewModel, que a su vez usa el ExoPlayerManager
                // para reemplazar completamente la cola de reproducción.
                viewModel.playPlaylist(songList, startIndex)
            } else {
                // Manejar el caso en que la songId solicitada no se encuentre en la lista de songIds.
                // Podrías navegar hacia atrás o mostrar un mensaje de error.
                // navController.popBackStack()
            }
        }
    }

    // Muestra un indicador de carga si no hay ninguna canción cargada
    if (currentSong == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    val isPlaying by viewModel.isPlaying // Observa el estado de reproducción del ViewModel

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                ),
                actions = {
                    IconButton(onClick = {
                        viewModel.toggleFavoriteStatus()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorito",
                            tint = if (isFavorite) Color.Red else Color.White
                        )
                    }

                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Imagen contenida en Card
            Card(
                modifier = Modifier
                    .size(300.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                val painter = rememberAsyncImagePainter(
                    model = if (currentSong!!.coverImage?.isEmpty() == true) R.drawable.ic_music_placeholder else currentSong!!.coverImage
                )
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Título con scroll horizontal
            Row(modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    currentSong!!.title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Artista con scroll horizontal
            Row(modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    currentSong!!.artistName,
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))


            Column(modifier = Modifier.fillMaxWidth()) {
                // Barra de progreso personalizada
                CustomThinSlider(
                    value = currentPosition.toFloat(),
                    onValueChange = { exoPlayer?.seekTo(it.toLong()) },
                    valueRange = 0f..duration.toFloat()
                )


                // Tiempo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatDuration(currentPosition), color = Color.White, fontSize = 12.sp)
                    Text(formatDuration(duration), color = Color.White, fontSize = 12.sp)
                }
            }


            Spacer(modifier = Modifier.height(24.dp))

            // Controles de reproducción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isShuffleEnabled by viewModel.isShuffleEnabled

                IconButton(onClick = { viewModel.toggleShuffleMode() }) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Aleatorio",
                        tint = if (isShuffleEnabled) Color.Cyan else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.playPrevious() },
                    enabled = canPlayPrevious
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", tint = Color.White, modifier = Modifier.size(36.dp))
                }

                IconButton(onClick = {
                    if (isPlaying) viewModel.pause() else viewModel.resume()
                }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.playNext() },
                    enabled = canPlayNext
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", tint = Color.White, modifier = Modifier.size(36.dp))
                }

                val isRepeatEnabled by viewModel.isRepeatEnabled

                IconButton(onClick = { viewModel.toggleRepeatMode() }) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Repetir",
                        tint = if (isRepeatEnabled) Color.Cyan else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }


            }
        }
    }

}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomThinSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        interactionSource = interactionSource,
        steps = 0,
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp), // Altura total del slider (incluyendo thumb)

        // 🎯 THUMB PERSONALIZADO (bolita)
        thumb = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(24.dp) // Espacio reservado para el thumb
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp) // Tamaño visible de la bolita
                        .background(Color(0xFFFF5000), shape = CircleShape) // 🎨 COLOR DEL THUMB (círculo)
                )
            }
        },

        // 🎯 TRACK PERSONALIZADO (línea)
        track = { sliderState ->
            val progressFraction = sliderState.value.coerceIn(0f..1f)

            // Fondo (parte no reproducida)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp) // Grosor de la barra
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Color(0xFF3A3A3A)) // 🎨 COLOR DE LA LÍNEA DE FONDO (inactiva)
            ) {
                // Progreso (parte reproducida) con degradado
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White,                      // 🎨 Inicio del gradiente
                                    Color(0xFFFF5000).copy(alpha = 0.3f) // 🎨 Fin translúcido
                                )
                            )
                        )
                )
            }
        },

        // 🎯 COLORES (se dejan transparentes porque el diseño es personalizado)
        colors = SliderDefaults.colors(
            thumbColor = Color.Unspecified,
            activeTrackColor = Color.Transparent,
            inactiveTrackColor = Color.Transparent
        )
    )
}



fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%02d:%02d", min, sec)
}