package com.example.pulseplayer.views.statistics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulseplayer.data.PulsePlayerDatabase
import com.example.pulseplayer.data.entity.GenrePlayStat
import com.example.pulseplayer.data.entity.NamedPlayStat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class StatsFilter(val label: String, val days: Int?) {
    Today("Hoy", 1), Week("7 días", 7), Month("30 días", 30), All("Todo", null)
}

private data class StatsUi(
    val plays: Int = 0,
    val listeningMs: Long = 0L,
    val uniqueSongs: Int = 0,
    val uniqueArtists: Int = 0,
    val topSong: NamedPlayStat? = null,
    val topArtist: NamedPlayStat? = null,
    val topGenre: NamedPlayStat? = null,
    val genres: List<GenrePlayStat> = emptyList(),
    val activityBars: List<Int> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalStatsScreen() {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf(StatsFilter.Week) }
    var stats by remember { mutableStateOf(StatsUi()) }

    LaunchedEffect(selectedFilter) {
        stats = withContext(Dispatchers.IO) {
            val dao = PulsePlayerDatabase.getDatabase(context).playbackHistoryDao()
            val from = selectedFilter.days?.let { daysAgo(it) }

            val plays = from?.let { dao.getCountSince(it) } ?: dao.getCount()
            val listeningMs = from?.let { dao.getListeningMsSince(it) } ?: dao.getTotalListeningMs()
            val uniqueSongs = from?.let { dao.getUniqueTracksSince(it) } ?: dao.getUniqueTracksCount()
            val uniqueArtists = from?.let { dao.getUniqueArtistsSince(it) } ?: dao.getUniqueArtistsCount()
            val topSong = from?.let { dao.getTopSongStatSince(it) } ?: dao.getTopSongStat()
            val topArtist = from?.let { dao.getTopArtistStatSince(it) } ?: dao.getTopArtistStat()
            val topGenre = from?.let { dao.getTopGenreStatSince(it) } ?: dao.getTopGenreStat()
            val genres = from?.let { dao.getGenreDistributionSince(it) } ?: dao.getGenreDistribution()

            val bars = generateDaySeries(selectedFilter.days ?: 7).map { prefix -> dao.getCountForDay(prefix) }

            StatsUi(
                plays = plays,
                listeningMs = listeningMs,
                uniqueSongs = uniqueSongs,
                uniqueArtists = uniqueArtists,
                topSong = topSong,
                topArtist = topArtist,
                topGenre = topGenre,
                genres = genres,
                activityBars = bars
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(Color(0xFF0F0F1A), Color(0xFF1A1A2E), Color(0xFF16162A))))
    ) {
        TopAppBar(
            title = {
                Column {
                    Text("Estadísticas", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Local • Sin streaming", color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FilterChips(selectedFilter) { selectedFilter = it }

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(400)) + slideInVertically(animationSpec = tween(400), initialOffsetY = { it / 2 })
            ) {
                QuickStatsGrid(stats)
            }

            GlassCard {
                Text("Actividad", color = Color.White, fontWeight = FontWeight.SemiBold)
                ActivityBarsChart(stats.activityBars)
            }

            TopCardsSection(stats)

            GlassCard {
                Text("Distribución por género", color = Color.White, fontWeight = FontWeight.SemiBold)
                GenreDistribution(stats.genres)
            }
        }
    }
}

@Composable
private fun FilterChips(selected: StatsFilter, onSelected: (StatsFilter) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(StatsFilter.entries) { chip ->
            val active = chip == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (active) Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFF3B82F6)))
                        else Brush.horizontalGradient(listOf(Color(0x26FFFFFF), Color(0x14FFFFFF)))
                    )
                    .clickable { onSelected(chip) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = chip.label,
                    color = if (active) Color.White else Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun QuickStatsGrid(stats: StatsUi) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MiniStatCard("Tiempo escuchado", formatDuration(stats.listeningMs), Modifier.weight(1f))
            MiniStatCard("Reproducciones", stats.plays.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MiniStatCard("Canciones distintas", stats.uniqueSongs.toString(), Modifier.weight(1f))
            MiniStatCard("Artistas distintos", stats.uniqueArtists.toString(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun MiniStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    GlassCard(modifier) {
        Text(label, color = Color.White.copy(alpha = 0.58f), fontSize = 12.sp)
        Text(value, color = Color(0xFFA78BFA), fontWeight = FontWeight.Bold, fontSize = 24.sp)
    }
}

@Composable
private fun ActivityBarsChart(values: List<Int>) {
    val max = (values.maxOrNull() ?: 1).coerceAtLeast(1)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        values.takeLast(14).forEachIndexed { index, v ->
            val ratio by animateFloatAsState(
                targetValue = (v.toFloat() / max.toFloat()).coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 600, delayMillis = index * 30, easing = FastOutSlowInEasing),
                label = "bar"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height((18 + 98 * ratio).dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF7C3AED), Color(0xFF3B82F6))))
            )
        }
    }
}

@Composable
private fun TopCardsSection(stats: StatsUi) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TopProgressCard("Top Canción", stats.topSong, listOf(Color(0xFF7C3AED), Color(0xFF3B82F6)))
        TopProgressCard("Top Artista", stats.topArtist, listOf(Color(0xFFEC4899), Color(0xFFF43F5E)))
        TopProgressCard("Género Favorito", stats.topGenre, listOf(Color(0xFF10B981), Color(0xFF14B8A6)))
    }
}

@Composable
private fun TopProgressCard(title: String, stat: NamedPlayStat?, gradient: List<Color>) {
    val count = stat?.playCount ?: 0
    val progress by animateFloatAsState(
        targetValue = (count / 50f).coerceIn(0f, 1f),
        animationSpec = tween(900),
        label = "progress"
    )
    GlassCard {
        Text(title, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
        Text(stat?.label ?: "Sin datos", color = Color.White, fontWeight = FontWeight.SemiBold)
        Text("$count plays", color = Color.White.copy(alpha = 0.66f), fontSize = 12.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.horizontalGradient(gradient))
            )
        }
    }
}

@Composable
private fun GenreDistribution(genres: List<GenrePlayStat>) {
    val total = genres.sumOf { it.playCount }.coerceAtLeast(1)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(140.dp)) {
            var start = -90f
            genres.take(5).forEachIndexed { index, g ->
                val angle = (g.playCount.toFloat() / total) * 360f
                drawArc(
                    color = donutColor(index),
                    startAngle = start,
                    sweepAngle = angle,
                    useCenter = false,
                    topLeft = Offset(12f, 12f),
                    size = Size(size.width - 24f, size.height - 24f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 26f, cap = StrokeCap.Butt)
                )
                start += angle
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            genres.take(5).forEachIndexed { index, g ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(donutColor(index))
                    )
                    Text(
                        text = "  ${g.label}  ${(g.playCount * 100 / total)}%",
                        color = Color.White.copy(alpha = 0.82f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassCard(modifier: Modifier = Modifier, content: @Composable Column.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x141FFFFFFF)),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
    }
}

private fun donutColor(index: Int): Color = listOf(
    Color(0xFF7C3AED), Color(0xFF3B82F6), Color(0xFFEC4899), Color(0xFF10B981), Color(0xFF6B7280)
)[index % 5]

private fun formatDuration(ms: Long): String {
    if (ms <= 0L) return "0h 0m"
    val totalMinutes = ms / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return "${hours}h ${minutes}m"
}

private fun generateDaySeries(days: Int): List<String> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val c = Calendar.getInstance()
    return (0 until days).map {
        val s = sdf.format(c.time)
        c.add(Calendar.DAY_OF_YEAR, -1)
        s
    }.reversed()
}

private fun daysAgo(days: Int): String {
    val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -days) }
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(calendar.time)
}
