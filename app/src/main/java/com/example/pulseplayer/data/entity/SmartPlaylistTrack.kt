package com.example.pulseplayer.data.entity

import androidx.room.ColumnInfo

data class SmartPlaylistTrack(
    @ColumnInfo(name = "id_song") val songId: Int,
    val title: String,
    @ColumnInfo(name = "artist_name") val artistName: String,
    @ColumnInfo(name = "cover_image") val coverImage: String?,
    @ColumnInfo(name = "file_path") val filePath: String,
    @ColumnInfo(name = "duration_ms") val durationMs: Int,
    @ColumnInfo(name = "play_count") val playCount: Int = 0
)
