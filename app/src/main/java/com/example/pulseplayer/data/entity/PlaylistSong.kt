package com.example.pulseplayer.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playlist_song",
    foreignKeys = [
        ForeignKey(entity = Playlist::class, parentColumns = ["id"], childColumns = ["playlist_id"]),
        ForeignKey(entity = Song::class, parentColumns = ["id_song"], childColumns = ["id_song"])
    ],
    indices = [Index("playlist_id"), Index("id_song")]
)
data class PlaylistSong(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "playlist_id") val playlistId: Int,
    @ColumnInfo(name = "id_song") val songId: Int,
    @ColumnInfo(name = "song_order") val songOrder: Int
)
