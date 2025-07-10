package com.example.pulseplayer.views.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulseplayer.data.PulsePlayerDatabase
import com.example.pulseplayer.data.entity.Playlist
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PlaylistViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = PulsePlayerDatabase.getDatabase(application).playlistDao()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAll().collect {
                _playlists.value = it
            }
        }
    }

    fun addPlaylist(name: String) {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val newPlaylist = Playlist(name = name, createdAt = now)

        viewModelScope.launch {
            dao.insert(newPlaylist)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            dao.deletePlaylistbyId(playlistId)
        }
    }

    fun updatePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            dao.update(playlist)
        }
    }

    suspend fun getPlaylistById(playlistId: Int): Playlist? {
        return dao.getPlaylistById(playlistId)
    }
}