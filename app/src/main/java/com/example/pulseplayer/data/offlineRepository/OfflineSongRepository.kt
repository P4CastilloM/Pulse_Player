package com.example.pulseplayer.data.offlineRepository

import com.example.pulseplayer.data.dao.SongDao
import com.example.pulseplayer.data.entity.Song
import com.example.pulseplayer.data.repository.SongRepository
import kotlinx.coroutines.flow.Flow


class OfflineSongRepository(private val dao: SongDao) : SongRepository {
    override val allSongs: Flow<List<Song>> = dao.getAll()

    override suspend fun insert(song: Song) = dao.insert(song)
    override suspend fun update(song: Song) = dao.update(song)
    override suspend fun delete(song: Song) = dao.delete(song)
    override suspend fun getById(id: Int): Song? = dao.getById(id)
}
