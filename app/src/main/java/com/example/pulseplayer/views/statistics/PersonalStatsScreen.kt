package com.example.pulseplayer.views.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pulseplayer.data.PulsePlayerDatabase
import com.example.pulseplayer.data.entity.NamedPlayStat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalStatsScreen() {
    val context = LocalContext.current

    var totalPlays by remember { mutableStateOf(0) }
    var todayPlays by remember { mutableStateOf(0) }
    var uniqueTracks by remember { mutableStateOf(0) }
    var totalListeningMs by remember { mutableLongStateOf(0L) }
    var todayListeningMs by remember { mutableLongStateOf(0L) }
    var topSong by remember { mutableStateOf<NamedPlayStat?>(null) }
    var topArtist by remember { mutableStateOf<NamedPlayStat?>(null) }
    var topGenre by remember { mutableStateOf<NamedPlayStat?>(null) }
    var weeklyPlays by remember { mutableStateOf(0) }
    var uniqueArtists by remember { mutableStateOf(0) }
    var uniqueGenres by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val dao = PulsePlayerDatabase.getDatabase(context).playbackHistoryDao()
            val dayPrefix = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            totalPlays = dao.getCount()
            todayPlays = dao.getCountForDay(dayPrefix)
            uniqueTracks = dao.getUniqueTracksCount()
            totalListeningMs = dao.getTotalListeningMs()
            todayListeningMs = dao.getListeningMsForDay(dayPrefix)
            topSong = dao.getTopSongStat()
            topArtist = dao.getTopArtistStat()
            topGenre = dao.getTopGenreStat()
            weeklyPlays = dao.getCountSince(daysAgo(7))
            uniqueArtists = dao.getUniqueArtistsCount()
            uniqueGenres = dao.getUniqueGenresCount()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas personales", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF090B1A))
            )
        },
        containerColor = Color(0xFF060911)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StatsCard(
                title = "📊 Estadísticas personales",
                rows = listOf(
                    "Canción más escuchada" to statLine(topSong),
                    "Artista más repetido" to statLine(topArtist),
                    "Género favorito" to statLine(topGenre),
                    "Horas totales reproducidas" to formatDuration(totalListeningMs)
                )
            )

            StatsCard(
                title = "Resumen del día",
                rows = listOf(
                    "Reproducciones de hoy" to todayPlays.toString(),
                    "Tiempo de escucha hoy" to formatDuration(todayListeningMs)
                )
            )

            StatsCard(
                title = "Resumen total",
                rows = listOf(
                    "Reproducciones totales" to totalPlays.toString(),
                    "Reproducciones últimos 7 días" to weeklyPlays.toString(),
                    "Canciones distintas" to uniqueTracks.toString(),
                    "Artistas distintos" to uniqueArtists.toString(),
                    "Géneros distintos" to uniqueGenres.toString(),
                    "Tiempo total de escucha" to formatDuration(totalListeningMs)
                )
            )
        }
    }
}

@Composable
private fun StatsCard(title: String, rows: List<Pair<String, String>>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121B33)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
            rows.forEach { (label, value) ->
                Text("$label: $value", color = Color.White.copy(alpha = 0.86f))
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0L) return "0 min"
    val totalMinutes = ms / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes} min"
}

private fun statLine(stat: NamedPlayStat?): String {
    return if (stat == null) "Sin datos" else "${stat.label} (${stat.playCount})"
}

private fun daysAgo(days: Int): String {
    val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -days) }
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(calendar.time)
}
