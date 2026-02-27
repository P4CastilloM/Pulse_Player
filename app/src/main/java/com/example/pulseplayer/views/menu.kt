package com.example.pulseplayer.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pulseplayer.Albums
import com.example.pulseplayer.FavoriteScreen
import com.example.pulseplayer.Music
import com.example.pulseplayer.PlaybackHistoryScreen
import com.example.pulseplayer.PlaylistScreen
import com.example.pulseplayer.ui.components.MiniPlayerBar
import com.example.pulseplayer.views.viewmodel.PlaylistViewModel
import com.example.pulseplayer.views.viewmodel.SongViewModel

@Composable
fun MenuScreen(navController: NavController) {
    val songViewModel: SongViewModel = viewModel()
    val playlistViewModel: PlaylistViewModel = viewModel()
    val songs by songViewModel.allSongs.collectAsState()
    val playlists by playlistViewModel.playlists.collectAsState()

    val albumCount = songs.map { it.album?.trim().orEmpty() }
        .filter { it.isNotEmpty() }
        .distinct()
        .size

    val categories = listOf(
        LibraryCategory("Álbumes", "$albumCount álbumes", Icons.Outlined.Album, Color(0xFF8B5CF6)) { navController.navigate(Albums) },
        LibraryCategory("Listas", "${playlists.size} listas", Icons.Outlined.QueueMusic, Color(0xFF60A5FA)) { navController.navigate(PlaylistScreen) },
        LibraryCategory("Historial", "Recientes", Icons.Outlined.AccessTime, Color(0xFF34D399)) { navController.navigate(PlaybackHistoryScreen) },
        LibraryCategory("Favoritos", "${songs.count { it.isFavorite }} canciones", Icons.Outlined.FavoriteBorder, Color(0xFFFB7185)) { navController.navigate(FavoriteScreen) }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF090B1A), Color(0xFF05060D))))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeHeader()
            HeroMusicCard(onClick = { navController.navigate(Music) })

            Text(
                text = "BIBLIOTECA",
                color = Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 18.dp, bottom = 10.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false,
                modifier = Modifier.height(270.dp)
            ) {
                items(categories.size) { index ->
                    CategoryCard(categories[index])
                }
            }

            MiniPlayerBar(
                navController = navController,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            BottomNavStrip(
                onHomeClick = {},
                onSearchClick = { navController.navigate(Music) },
                onLibraryClick = { navController.navigate(Albums) },
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp)
                .width(132.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
        )
    }
}

@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFF4F46E5)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.GraphicEq, contentDescription = null, tint = Color.White)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4ADE80))
                        .border(2.dp, Color(0xFF090B1A), CircleShape)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Pulse Player", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                Text("Reproductor Local", color = Color.White.copy(alpha = 0.45f), fontSize = 13.sp)
            }
        }

        IconButton(
            onClick = {},
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
        ) {
            Icon(Icons.Outlined.Settings, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun HeroMusicCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF5B21B6), Color(0xFF1E3A8A), Color(0xFF0F172A))))
            .clickable(onClick = onClick)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
            }
            Text("Música", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
            Text("Explorar biblioteca", color = Color.White.copy(alpha = 0.6f), fontSize = 15.sp)
        }
    }
}

data class LibraryCategory(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
private fun CategoryCard(item: LibraryCategory) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF13203C), Color(0xFF0A132B))))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(18.dp))
            .clickable(onClick = item.onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(item.color.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.icon, contentDescription = null, tint = item.color)
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(item.title, color = Color.White, fontWeight = FontWeight.SemiBold)
        Text(item.subtitle, color = Color.White.copy(alpha = 0.45f), fontSize = 13.sp)
    }
}

@Composable
private fun BottomNavStrip(
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onLibraryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem("Inicio", Icons.Outlined.Home, true, onHomeClick)
        BottomNavItem("Buscar", Icons.Outlined.Search, false, onSearchClick)
        BottomNavItem("Biblioteca", Icons.Outlined.LibraryMusic, false, onLibraryClick)
    }
}

@Composable
private fun BottomNavItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(4.dp)
    ) {
        Icon(icon, contentDescription = label, tint = if (selected) Color(0xFF8B5CF6) else Color.White.copy(alpha = 0.38f))
        Text(
            text = label,
            color = if (selected) Color(0xFF8B5CF6) else Color.White.copy(alpha = 0.38f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}
