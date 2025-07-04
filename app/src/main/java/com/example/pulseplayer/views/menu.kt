package com.example.pulseplayer.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
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
import com.example.pulseplayer.R

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
            // Ya no hay título grande aquí (MÚSICA), solo tarjetas
            MusicCard(
                title = "Música",
                icon = Icons.Default.MusicNote,
                colors = listOf(Color(0xFFFF416C), Color(0xFFFF4B2B)),
                onClick = { navController.navigate(Music) }
            )



            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CategoryCard(
                    title = "Álbumes",
                    iconId = R.drawable.ic_album, // Asegúrate de tener este ícono en drawable
                    colors = listOf(Color(0xFF2193b0), Color(0xFF6dd5ed)),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                CategoryCard(
                    title = "Categorías",
                    iconId = R.drawable.ic_category, // Asegúrate de tener este ícono en drawable
                    colors = listOf(Color(0xFF56ab2f), Color(0xFFA8E063)),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
fun CategoryCard(title: String, iconId: Int, colors: List<Color>, modifier: Modifier) {
    Box(
        modifier = modifier
            .height(100.dp)
            .background(
                brush = Brush.horizontalGradient(colors),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { },
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