package com.example.pulseplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.FastRewind
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
        onDispose { player?.removeListener(listener) }
    }

    val song = currentSong.value ?: return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(74.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF171A2C), Color(0xFF121527))))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            .clickable { navController.navigate(NowPlaying(song.idSong, listOf(song.idSong))) }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.coverImage?.ifEmpty { R.drawable.ic_music_placeholder },
            contentDescription = null,
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(song.title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artistName, color = Color.White.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        IconButton(onClick = { ExoPlayerManager.playPrevious() }) {
            Icon(Icons.Outlined.FastRewind, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
        }
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF4F46E5)))),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = {
                if (isPlaying.value) ExoPlayerManager.pause() else ExoPlayerManager.resume()
            }) {
                Icon(if (isPlaying.value) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White)
            }
        }
        IconButton(onClick = { ExoPlayerManager.playNext() }) {
            Icon(Icons.Outlined.FastForward, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
        }
    }
}
