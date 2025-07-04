package com.example.pulseplayer

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pulseplayer.views.MenuScreen
import com.example.pulseplayer.views.MusicScreen
import kotlinx.serialization.Serializable

@Serializable
object Menu

@Serializable
object Music

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
    }
}