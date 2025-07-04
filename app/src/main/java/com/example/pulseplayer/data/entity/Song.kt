package com.example.pulseplayer.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "song")
data class Song(
    @PrimaryKey @ColumnInfo(name = "id_song") val idSong: Int,
    val title: String,
    @ColumnInfo(name = "artist_name") val artistName: String,
    val album: String?,
    @ColumnInfo(name = "track_number") val trackNumber: String?,
    val genre: String?,
    @ColumnInfo(name = "release_year") val releaseYear: String?,
    @ColumnInfo(name = "cover_image") val coverImage: String?,
    @ColumnInfo(name = "duration_ms") val durationMs: Int,
    @ColumnInfo(name = "formatted_duration") val formattedDuration: String,
    @ColumnInfo(name = "file_path") val filePath: String
)
