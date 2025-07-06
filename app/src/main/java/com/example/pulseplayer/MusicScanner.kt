package com.example.pulseplayer.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Log
import com.example.pulseplayer.data.dao.SongDao
import com.example.pulseplayer.data.entity.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object MusicScanner {

    suspend fun scanAndSyncSongs(context: Context, dao: SongDao) = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver

        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND " +
                "(${MediaStore.Audio.Media.DATA} LIKE ? OR ${MediaStore.Audio.Media.DATA} LIKE ?)"
        val selectionArgs = arrayOf("%/Music/%", "%/Download/%")

        val cursor = contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            null
        ) ?: return@withContext

        val foundPaths = mutableSetOf<String>()

        cursor.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val trackCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val yearCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val durationCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            Log.d("MusicScanner", "🎧 Total canciones encontradas: ${it.count}")
            while (it.moveToNext()) {
                val path = it.getString(dataCol) ?: continue
                val supportedExtensions = listOf(".mp3", ".aac", ".m4a", ".wav", ".ogg", ".flac")
                if (supportedExtensions.none { path.endsWith(it, ignoreCase = true) }) continue


                Log.d("MusicScanner", "🎵 Ruta encontrada: $path")
                foundPaths.add(path)

                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(path)

                val genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE) ?: ""
                val embeddedPicture = retriever.embeddedPicture
                val coverImageUri = if (embeddedPicture != null) {
                    val bitmap = BitmapFactory.decodeByteArray(embeddedPicture, 0, embeddedPicture.size)
                    val file = File(context.cacheDir, "cover_${path.hashCode()}.jpg")
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    file.toURI().toString()
                } else {
                    ""
                }

                retriever.release()

                val song = Song(
                    idSong = it.getInt(idCol),
                    title = it.getString(titleCol) ?: "Unknown",
                    artistName = it.getString(artistCol) ?: "Unknown",
                    album = it.getString(albumCol) ?: "",
                    trackNumber = it.getString(trackCol) ?: "",
                    genre = genre,
                    releaseYear = it.getString(yearCol) ?: "",
                    coverImage = coverImageUri,
                    durationMs = it.getInt(durationCol),
                    formattedDuration = formatDuration(it.getInt(durationCol)),
                    filePath = path
                )

                dao.insert(song)
            }
        }

        // Elimina canciones que ya no existen
        val songsInDb = dao.getAll().first()
        for (song in songsInDb) {
            if (!foundPaths.contains(song.filePath)) {
                dao.delete(song)
            }
        }
    }

    private fun formatDuration(ms: Int): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format("%02d:%02d", min, sec)
    }
}
