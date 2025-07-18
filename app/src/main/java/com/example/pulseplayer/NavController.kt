package com.example.pulseplayer

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.pulseplayer.data.entity.Song
import com.example.pulseplayer.views.MenuScreen
import com.example.pulseplayer.views.MusicScreen
import com.example.pulseplayer.views.favorite.FavoriteScreen
import com.example.pulseplayer.views.player.NowPlayingScreen
import com.example.pulseplayer.views.playbackhistory.PlaybackHistoryScreen
import com.example.pulseplayer.views.playlist.AddSongsToPlaylistScreen
import com.example.pulseplayer.views.playlist.PlaylistCreateScreen
import com.example.pulseplayer.views.playlist.PlaylistDetailsScreen
import com.example.pulseplayer.views.playlist.PlaylistScreen
import com.example.pulseplayer.views.viewmodel.PlayerViewModel
import kotlinx.serialization.Serializable

//PRINCIPAL
@Serializable
object Menu

// MUSICA
@Serializable
object Music

@Serializable
data class NowPlaying(val songId: Int, val songIds: List<Int> )

//PLAYLIST
@Serializable
object PlaylistScreen

@Serializable
object PlaylistCreateScreen

@Serializable
data class PlaylistDetailScreen(val playlistId: Int)

@Serializable
data class AddSongsToPlaylistScreen(val playlistId: Int)

//PlaybackHistory
@Serializable
object PlaybackHistoryScreen

//FAVORITE
@Serializable
object FavoriteScreen



@Composable
fun Navigation(songs: List<Song>){
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    NavHost(navController = navController, startDestination = Menu){
        composable<Menu>{
            MenuScreen(navController = navController)
        }
        composable<Music> {
            MusicScreen(navController = navController) // ✅ corregido
        }
        composable<NowPlaying> { backStackEntry ->
            val args = backStackEntry.toRoute<NowPlaying>()
            NowPlayingScreen(navController = navController, songId = args.songId, songIds = args.songIds,viewModel = playerViewModel)
        }
        composable<PlaylistScreen> {
            PlaylistScreen(navController)
        }
        composable<PlaylistCreateScreen> {
            PlaylistCreateScreen(navController)
        }
        // composable<PlaylistDetailScreen> { backStackEntry ->
        //     val args = backStackEntry.toRoute<PlaylistDetailScreen>()
        //     PlaylistDetailScreen(navController, playlistId = args.playlistId)
        // }

        composable<PlaybackHistoryScreen> {
            PlaybackHistoryScreen(navController, playerViewModel = playerViewModel)
        }

        composable<FavoriteScreen> {
            FavoriteScreen(navController = navController)
        }

        composable<PlaylistDetailScreen> {backStackEntry ->
            val args = backStackEntry.toRoute<PlaylistDetailScreen>()
            PlaylistDetailsScreen(navController = navController,playlistId = args.playlistId)
        }

        composable<AddSongsToPlaylistScreen> { backStackEntry ->
            val args = backStackEntry.toRoute<AddSongsToPlaylistScreen>()
            AddSongsToPlaylistScreen(navController = navController, playlistId = args.playlistId)
        }



    }
}