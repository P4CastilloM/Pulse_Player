package com.example.pulseplayer.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playback_history",
    foreignKeys = [
        ForeignKey(entity = Song::class, parentColumns = ["id_song"], childColumns = ["id_song"])
    ],
    indices = [Index("id_song")]
)
data class PlaybackHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "id_song") val songId: Int,
    @ColumnInfo(name = "played_at") val playedAt: String // o Date con TypeConverter
)
