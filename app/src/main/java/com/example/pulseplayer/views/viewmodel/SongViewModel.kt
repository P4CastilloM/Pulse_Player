package com.example.pulseplayer.views.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseplayer.data.PulsePlayerDatabase
import com.example.pulseplayer.data.entity.Song
import com.example.pulseplayer.data.offlineRepository.OfflineSongRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SongViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = PulsePlayerDatabase.getDatabase(application).songDao()
    private val repository = OfflineSongRepository(dao)

    // Lista completa de canciones
    val allSongs = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Solo canciones favoritas
    val favoriteSongs = repository.favoriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Obtener una canción por ID (suspend fun)
    suspend fun getSongById(id: Int): Song? {
        return repository.getById(id)
    }

    // Cambiar el estado de favorito
    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val updated = song.copy(isFavorite = !song.isFavorite)
            repository.update(updated)
        }
    }

    // También puedes exponer funciones de inserción o eliminación si lo necesitas
    fun insertSong(song: Song) {
        viewModelScope.launch {
            repository.insert(song)
        }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            repository.delete(song)
        }
    }
}
