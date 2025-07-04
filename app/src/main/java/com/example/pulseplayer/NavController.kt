package com.example.pulseplayer

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.pulseplayer.views.MenuScreen
import com.example.pulseplayer.views.MusicScreen
import com.example.pulseplayer.views.NowPlayingScreen
import kotlinx.serialization.Serializable

@Serializable
object Menu

@Serializable
object Music

@Serializable
data class NowPlaying(val songId: Int, val songIds: List<Int>)

@Composable
fun Navigation(){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Menu){
        composable<Menu>{
            MenuScreen(navController = navController)
        }
        composable<Music> {
            MusicScreen(navController = navController)
        }
        composable<NowPlaying> { backStackEntry ->
            val args = backStackEntry.toRoute<NowPlaying>()
            NowPlayingScreen(navController = navController, songId = args.songId, songIds = args.songIds)
        }
    }
}