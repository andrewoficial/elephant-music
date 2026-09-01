package ru.kantser.elephantmusic.platform

import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path

actual fun readAudioMetadata(path: String): AudioMetadata {
    return try {
        val file = File(path)
        if (!file.isFile) return AudioMetadata()
        val bytes = readId3Region(file) ?: return AudioMetadata()
        Id3Parser.parse(bytes, path) { data, srcPath -> saveArtwork(data, srcPath) }
    } catch (e: Exception) {
        AudioMetadata()
    }
}

/** Читает заголовок ID3v2 и байты тега (до ~512 КБ, чего достаточно для тегов/обложек). */
private fun readId3Region(file: File): ByteArray? {
    val raf = RandomAccessFile(file, "r")
    try {
        if (file.length() < 10) return null
        val head = ByteArray(10)
        raf.readFully(head)
        if (head[0].toInt().toChar() != 'I' || head[1].toInt().toChar() != 'D' || head[2].toInt().toChar() != '3') {
            return head // файл без ID3 — всё равно отдадим, парсер не найдёт тег
        }
        val size = synchsafe(head, 6)
        val toRead = (10 + size).coerceAtMost(512 * 1024)
        val bytes = ByteArray(toRead.toInt())
        raf.seek(0)
        raf.readFully(bytes)
        return bytes
    } finally {
        raf.close()
    }
}

private fun synchsafe(b: ByteArray, off: Int): Int =
    ((b[off].toInt() and 0x7F) shl 21) or
        ((b[off + 1].toInt() and 0x7F) shl 14) or
        ((b[off + 2].toInt() and 0x7F) shl 7) or
        (b[off + 3].toInt() and 0x7F)

/** Сохраняет извлечённую обложку в кэш-папку приложения и возвращает путь файла. */
private fun saveArtwork(data: ByteArray, srcPath: String): String? {
    return try {
        val ext = artworkExt(data)
        val cache = Path.of(System.getProperty("user.home"), ".ElephantPlayer", "artwork")
        Files.createDirectories(cache)
        val name = srcPath.substringAfterLast('/').substringAfterLast('\\').hashCode().toString() + ext
        val target = cache.resolve(name)
        if (!Files.exists(target)) Files.write(target, data)
        target.toAbsolutePath().toString()
    } catch (e: Exception) {
        null
    }
}

private fun artworkExt(data: ByteArray): String = when {
    data.size > 3 && data[0].toInt().toChar() == 'P' && data[1].toInt().toChar() == 'N' && data[2].toInt().toChar() == 'G' -> ".png"
    data.size > 2 && (data[0].toInt() and 0xFF) == 0xFF && (data[1].toInt() and 0xFF) == 0xD8 -> ".jpg"
    data.size > 3 && (data[0].toInt() and 0xFF) == 0x49 && (data[1].toInt() and 0xFF) == 0x49 -> ".tiff"
    else -> ".bin"
}
