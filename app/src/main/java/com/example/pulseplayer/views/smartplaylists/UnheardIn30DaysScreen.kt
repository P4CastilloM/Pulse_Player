package com.example.pulseplayer.views.smartplaylists

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.pulseplayer.data.PulsePlayerDatabase
import com.example.pulseplayer.data.entity.SmartPlaylistTrack
import com.example.pulseplayer.views.viewmodel.PlayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnheardIn30DaysScreen(navController: NavController, playerViewModel: PlayerViewModel) {
    val context = LocalContext.current
    var tracks by remember { mutableStateOf<List<SmartPlaylistTrack>>(emptyList()) }

    LaunchedEffect(Unit) {
        tracks = withContext(Dispatchers.IO) {
            PulsePlayerDatabase.getDatabase(context).playbackHistoryDao().getNotPlayedSince(daysAgo30())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("No escuchadas en 30 días", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF090B1A))
            )
        },
        containerColor = Color(0xFF060911)
    ) { padding ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
        ) {
            items(tracks) { track ->
                SmartTrackCard(track = track, onClick = {})
            }
        }
    }
}

private fun daysAgo30(): String {
    val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(calendar.time)
}
