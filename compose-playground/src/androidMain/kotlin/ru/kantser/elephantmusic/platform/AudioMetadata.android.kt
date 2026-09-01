package ru.kantser.elephantmusic.platform

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import ru.kantser.elephantmusic.AppContextHolder
import java.io.File

actual fun readAudioMetadata(path: String): AudioMetadata {
    return try {
        val mmr = MediaMetadataRetriever()
        try {
            if (path.startsWith("content://")) {
                mmr.setDataSource(AppContextHolder.context, Uri.parse(path))
            } else {
                mmr.setDataSource(path)
            }
            AudioMetadata(
                title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "",
                artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "",
                album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "",
                year = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR) ?: "",
                trackNumber = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER) ?: "",
                genre = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE) ?: "",
                composer = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER) ?: "",
                lyrics = null,
                coverArtPath = mmr.embeddedPicture?.let { saveArtwork(it, path) },
            )
        } finally {
            mmr.release()
        }
    } catch (e: Exception) {
        AudioMetadata()
    }
}

/** Сохраняет обложку из тега в кэш приложения и возвращает путь файла. */
private fun saveArtwork(data: ByteArray, srcPath: String): String? {
    return try {
        val dir = File(AppContextHolder.context.cacheDir, "artwork")
        dir.mkdirs()
        val ext = when {
            data.size > 3 && data[0].toInt().toChar() == 'P' &&
                data[1].toInt().toChar() == 'N' && data[2].toInt().toChar() == 'G' -> ".png"
            else -> ".jpg"
        }
        val name = srcPath.hashCode().toString() + ext
        val target = File(dir, name)
        if (!target.exists()) target.writeBytes(data)
        target.absolutePath
    } catch (e: Exception) {
        null
    }
}
