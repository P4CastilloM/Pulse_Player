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
fun NowPlayingScreen(navController: NavController, songId: Int, songIds: List<Int>, viewModel: PlayerViewModel) {
    val context = LocalContext.current
    val currentSong by viewModel.currentSong

    val exoPlayer = viewModel.getPlayer()
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }



    // Actualiza progreso
    LaunchedEffect(Unit) {
        while (true) {
            exoPlayer?.let {
                currentPosition = it.currentPosition
                duration = it.duration.takeIf { d -> d > 0 } ?: 1L
            }
            delay(500)
        }
    }

    // Carga canción si no hay nada cargado
    LaunchedEffect(songId, songIds) {
        //if (currentSong == null) {
            val dao = PulsePlayerDatabase.getDatabase(context).songDao()
            val songList = songIds.map { dao.getById(it) }
            val startIndex = songList.indexOfFirst { it?.idSong == songId }

            //if (startIndex != -1) {
                // Evitamos duplicar historial si ya se reprodujo antes
                viewModel.playPlaylist(songList as List<Song>, startIndex, saveHistory = false)
            //}
        //}
    }


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

    val isPlaying by viewModel.isPlaying

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
                IconButton(onClick = { viewModel.playPrevious() }) {
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

                IconButton(onClick = { viewModel.playNext() }) {
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
