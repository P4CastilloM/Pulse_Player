package com.example.pulseplayer.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pulseplayer.data.dao.PlaybackHistoryDao
import com.example.pulseplayer.data.dao.PlaylistDao
import com.example.pulseplayer.data.dao.PlaylistSongDao
import com.example.pulseplayer.data.dao.SongDao
import com.example.pulseplayer.data.entity.PlaybackHistory
import com.example.pulseplayer.data.entity.Playlist
import com.example.pulseplayer.data.entity.PlaylistSong
import com.example.pulseplayer.data.entity.Song

@Database(
    entities = [Playlist::class, Song::class, PlaylistSong::class, PlaybackHistory::class],
    version = 3,
    exportSchema = false
)
abstract class PulsePlayerDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun songDao(): SongDao
    abstract fun playlistSongDao(): PlaylistSongDao
    abstract fun playbackHistoryDao(): PlaybackHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: PulsePlayerDatabase? = null

        fun getDatabase(context: Context): PulsePlayerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PulsePlayerDatabase::class.java,
                    "pulse_player_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            db.execSQL("PRAGMA foreign_keys=ON;")
                            Log.d("PulsePlayerDatabase", "✅ Base de datos abierta y claves foráneas activadas")
                        }

                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d("PulsePlayerDatabase", "🎉 Base de datos creada por primera vez")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
