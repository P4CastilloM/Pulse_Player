package com.example.pulseplayer.views

import android.util.Log
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pulseplayer.FavoriteScreen
import com.example.pulseplayer.Music
import com.example.pulseplayer.PlaybackHistoryScreen
import com.example.pulseplayer.PlaylistScreen
import com.example.pulseplayer.R
import com.example.pulseplayer.isLandscape
import com.example.pulseplayer.ui.components.MiniPlayerBar
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(navController: NavController) {
    //orientacion de la pantalla
    val landscape = isLandscape()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
            Log.d("Color:","${MaterialTheme.colorScheme.primary}")
        },
        bottomBar = {
            MiniPlayerBar(
                navController = navController,
                modifier = Modifier.fillMaxWidth().wrapContentHeight().navigationBarsPadding(),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        val modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp)

        if (landscape) {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                //musiccard a la izquierda
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Top
                ) {
                    MusicCard(
                        title = stringResource(R.string.title_music),
                        icon = Icons.Default.MusicNote,
                        colors = listOf(Color(0xFFFF416C), Color(0xFFFF4B2B)),
                        onClick = { navController.navigate(Music) },
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                    )
                }
                //categorias ordenables a la derecha
                Column(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                ) {
                    ReorderableCategorySection(navController = navController)
                }
            }
        } else {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MusicCard(
                    title = stringResource(R.string.title_music),
                    icon = Icons.Default.MusicNote,
                    colors = listOf(Color(0xFFFF416C), Color(0xFFFF4B2B)),
                    onClick = { navController.navigate(Music) }
                )

                ReorderableCategorySection(navController = navController)

            }
        }
    }
}

@Composable
fun MusicCard(
    title: String,
    icon: ImageVector,
    colors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(100.dp)) {
    Box(
        modifier = modifier
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
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
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
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReorderableCategorySection(navController: NavController) {
    val items = remember { mutableStateListOf("Álbumes", "Listas", "Historial", "Favorito") }
    val landscape = isLandscape()

    val state = rememberReorderableLazyGridState(onMove = { from, to ->
        val item = items.removeAt(from.index)
        items.add(to.index, item)
    })

    val columns = if (landscape) 3 else 2 //si es horizontal 3 columnas, si es vertical 2

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        state = state.gridState,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (landscape) 200.dp else 240.dp)
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
                    .aspectRatio(if (landscape) 1.5f else 2f)
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

    val onClick: () -> Unit = {
        when (title) {
            "Listas" -> navController?.navigate(PlaylistScreen)
            "Historial" -> navController?.navigate(PlaybackHistoryScreen)
            "Favorito" -> navController?.navigate(FavoriteScreen) // 🔸 Aquí se agregó
            else -> {}
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