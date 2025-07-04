package com.example.pulseplayer.data.repository

import com.example.pulseplayer.data.entity.PlaybackHistory
import kotlinx.coroutines.flow.Flow

interface PlaybackHistoryRepository {
    val allHistory: Flow<List<PlaybackHistory>>
    suspend fun insert(history: PlaybackHistory)
    suspend fun update(history: PlaybackHistory)
    suspend fun delete(history: PlaybackHistory)
}
