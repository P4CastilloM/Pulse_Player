package com.example.pulseplayer.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pulseplayer.Music
import com.example.pulseplayer.PlaylistScreen
import com.example.pulseplayer.R
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.reorderable


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PulsePlayer",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            MusicCard(
                title = "Música",
                icon = Icons.Default.MusicNote,
                colors = listOf(Color(0xFFFF416C), Color(0xFFFF4B2B)),
                onClick = { navController.navigate(Music) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ReorderableCategorySection(navController = navController)

            SongCard(
                title = "Blinding Lights",
                artist = "The Weeknd",
                colors = listOf(Color(0xFF833ab4), Color(0xFFfd1d1d), Color(0xFFfcb045))
            )
        }
    }
}

@Composable
fun MusicCard(title: String, icon: ImageVector, colors: List<Color>, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(
                brush = Brush.horizontalGradient(colors),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CategoryCard(
    title: String,
    iconId: Int,
    colors: List<Color>,
    modifier: Modifier,
    onClick: () -> Unit
){
    Box(
        modifier = modifier
            .height(100.dp)
            .background(
                brush = Brush.horizontalGradient(colors),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },  // ✅ ejecuta el onClick real
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SongCard(title: String, artist: String, colors: List<Color>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(
                brush = Brush.horizontalGradient(colors),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = artist,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReorderableCategorySection(navController: NavController) {
    val items = remember { mutableStateListOf("Álbumes", "Listas", "Historial", "Favorito") }

    val state = rememberReorderableLazyGridState(onMove = { from, to ->
        val item = items.removeAt(from.index)
        items.add(to.index, item)
    })

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = state.gridState,
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .reorderable(state)
            .detectReorderAfterLongPress(state),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 8.dp)
    ) {
        items(items.size, { items[it] }) { index ->
            val title = items[index]
            ReorderableItem(state, key = title) {
                val cardModifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f)
                    .animateItemPlacement()

                CategoryCardDynamic(title = title, modifier = cardModifier, navController = navController)
            }
        }
    }
}


@Composable
fun CategoryCardDynamic(
    title: String,
    modifier: Modifier = Modifier,
    navController: NavController? = null
) {
    val (iconId, colors) = when (title) {
        "Álbumes" -> R.drawable.ic_album to listOf(Color(0xFF2193b0), Color(0xFF6dd5ed))
        "Listas" -> R.drawable.ic_category to listOf(Color(0xFF56ab2f), Color(0xFFA8E063))
        "Historial" -> R.drawable.ic_history to listOf(Color(0xFF7F00FF), Color(0xFFE100FF))
        "Favorito" -> R.drawable.ic_favorite to listOf(Color(0xFFff6a00), Color(0xFFee0979))
        else -> R.drawable.ic_album to listOf(Color.Gray, Color.LightGray)
    }

    val onClick = {
        if (title == "Listas") {
            navController?.navigate(PlaylistScreen)
        }
    }

    CategoryCard(
        title = title,
        iconId = iconId,
        colors = colors,
        modifier = modifier,
        onClick = onClick
    )

}
