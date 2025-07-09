package com.example.pulseplayer.data.offlineRepository

import com.example.pulseplayer.data.dao.PlaybackHistoryDao
import com.example.pulseplayer.data.entity.PlaybackHistory
import com.example.pulseplayer.data.repository.PlaybackHistoryRepository
import kotlinx.coroutines.flow.Flow

class OfflinePlaybackHistoryRepository(private val dao: PlaybackHistoryDao) :
    PlaybackHistoryRepository {
    override val allHistory: Flow<List<PlaybackHistory>> = dao.getAll()

    override suspend fun insert(history: PlaybackHistory) = dao.insert(history)
    override suspend fun update(history: PlaybackHistory) = dao.update(history)
    override suspend fun delete(history: PlaybackHistory) = dao.delete(history)
    override suspend fun deleteAll() = dao.deleteAll() // 👈 Nueva función
}
