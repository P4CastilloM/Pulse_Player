package com.example.pulseplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.pulseplayer.NowPlaying
import com.example.pulseplayer.R
import com.example.pulseplayer.views.player.ExoPlayerManager

@Composable
fun MiniPlayerBar(navController: NavController, modifier: Modifier = Modifier) {
    val currentSong = remember { mutableStateOf(ExoPlayerManager.getCurrentSong()) }
    val isPlaying = remember { mutableStateOf(ExoPlayerManager.getPlayer()?.isPlaying == true) }

    // Listener de cambios en reproducción
    DisposableEffect(Unit) {
        val player = ExoPlayerManager.getPlayer()
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying.value = isPlayingNow
                currentSong.value = ExoPlayerManager.getCurrentSong()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentSong.value = ExoPlayerManager.getCurrentSong()
            }
        }
        player?.addListener(listener)

        onDispose {
            player?.removeListener(listener)
        }
    }


    val song = currentSong.value ?: return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(
                MaterialTheme.colorScheme.primary
            )
            .clickable {
                navController.navigate(
                    NowPlaying(song.idSong, listOf(song.idSong))
                )
            }
            .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = song.coverImage?.ifEmpty { R.drawable.ic_music_placeholder },
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    song.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
//                    modifier = Modifier.offset(y = (3).dp)
                )
                Text(
                    text = song.artistName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
//                    modifier = Modifier.offset(y = (-3).dp)
                )
            }

            IconButton(onClick = {
                if (isPlaying.value) ExoPlayerManager.pause() else ExoPlayerManager.resume()
            }) {
                Icon(
                    imageVector = if (isPlaying.value) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}