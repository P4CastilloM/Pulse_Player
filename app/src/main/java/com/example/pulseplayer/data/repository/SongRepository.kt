package com.example.pulseplayer.data.repository

import com.example.pulseplayer.data.entity.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    val allSongs: Flow<List<Song>>
    suspend fun insert(song: Song)
    suspend fun update(song: Song)
    suspend fun delete(song: Song)
    suspend fun getById(id: Int): Song?
}
