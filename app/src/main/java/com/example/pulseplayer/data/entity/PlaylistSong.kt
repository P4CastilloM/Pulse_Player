package com.example.pulseplayer.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playlist_song",
    foreignKeys = [
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["id"],
            childColumns = ["playlist_id"],
            onDelete = ForeignKey.CASCADE // <--- al borrar la playlist, elimina las canciones asociadas
        ),
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id_song"],
            childColumns = ["id_song"],
            onDelete = ForeignKey.CASCADE // <--- opcional: elimina esta fila si se elimina la canción
        )
    ],
    indices = [Index("playlist_id"), Index("id_song")]
)
data class PlaylistSong(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "playlist_id") val playlistId: Int,
    @ColumnInfo(name = "id_song") val songId: Int,
    @ColumnInfo(name = "song_order") val songOrder: Int
)