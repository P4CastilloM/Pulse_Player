package com.example.pulseplayer.views.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    Scaffold(containerColor = Color.Black) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            val painter = rememberAsyncImagePainter(
                model = if (currentSong!!.coverImage?.isEmpty() == true) R.drawable.ic_music_placeholder else currentSong!!.coverImage
            )

            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(currentSong!!.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(currentSong!!.artistName, color = Color.Gray, fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp))

            Spacer(modifier = Modifier.height(32.dp))

            Slider(
                value = currentPosition.toFloat(),
                onValueChange = { exoPlayer?.seekTo(it.toLong()) },
                valueRange = 0f..duration.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.Gray
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatDuration(currentPosition), color = Color.White, fontSize = 12.sp)
                Text(formatDuration(duration), color = Color.White, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.playPrevious() },
                    enabled = canPlayPrevious // Deshabilita si no hay canción anterior
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
                    enabled = canPlayNext // Deshabilita si no hay canción siguiente
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", tint = Color.White, modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}

fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%02d:%02d", min, sec)
}