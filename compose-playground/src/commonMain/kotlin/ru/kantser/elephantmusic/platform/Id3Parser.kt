package ru.kantser.elephantmusic.platform

import kotlin.math.max
import kotlin.math.min

/**
 * Лёгкий кросс-платформенный парсер ID3v2 (2.2 / 2.3 / 2.4) работает с уже прочитанным
 * массивом байт начала аудиофайла. Извлекает текстовые теги (TIT2, TPE1, TALB, …) и обложку
 * (APIC/PIC). Не требует внешних библиотек, поэтому одинаково работает на desktop и android.
 */
internal object Id3Parser {

    private const val ID3 = "ID3"

    /** Текстовые фреймы с нормализованным ключом (без 'T'/'') -> дескриптор поля. */
    private val textFrames = mapOf(
        "TIT2" to "title",
        "TT2" to "title",
        "TPE1" to "artist",
        "TP1" to "artist",
        "TALB" to "album",
        "TAL" to "album",
        "TYER" to "year",
        "TYE" to "year",
        "TDRC" to "year",
        "TRCK" to "track",
        "TRK" to "track",
        "TCON" to "genre",
        "TCO" to "genre",
        "TCOM" to "composer",
        "TCM" to "composer",
    )

    /**
     * Разбирает начало аудиофайла (копия достаточно большого куска, чтобы покрыть тег).
     * [path] — реальный путь/URI, используется при сохранении извлечённой обложки.
     * [saveArtwork] — колбэк для сохранения обложки в кэш; возвращает путь файла или null.
     */
    fun parse(
        bytes: ByteArray,
        path: String,
        saveArtwork: (ByteArray, String) -> String?,
    ): AudioMetadata {
        val frames = parseFrames(bytes)
        if (frames.isEmpty()) return AudioMetadata()

        val title = frameText(frames, "TIT2", "TT2")
        val artist = frameText(frames, "TPE1", "TP1")
        val album = frameText(frames, "TALB", "TAL")
        val year = frameText(frames, "TYER", "TDRC", "TYE")
        val track = frameText(frames, "TRCK", "TRK")
        val genre = genreText(frameText(frames, "TCON", "TCO"))
        val composer = frameText(frames, "TCOM", "TCM")
        val lyrics = frameText(frames, "USLT", "ULT")

        val artwork = (imageFrames(bytes, frames).firstOrNull()?.let { data ->
            saveArtwork(data, path)
        } ?: "").takeIf { it.isNotEmpty() }

        return AudioMetadata(
            title = title,
            artist = artist,
            album = album,
            year = year,
            trackNumber = track,
            genre = genre,
            composer = composer,
            lyrics = lyrics.ifEmpty { null },
            coverArtPath = artwork,
        )
    }

    /** Разбирает фреймы ID3v2 в массив (id, payload). */
    private fun parseFrames(bytes: ByteArray): List<Frame> {
        if (bytes.size < 10 || decodeAscii(bytes, 0, 3) != ID3) return emptyList()
        val major = bytes[3].toInt() and 0xFF
        val flags = bytes[5].toInt() and 0xFF
        // Размер тега — «синчсейф» (7 бит на байт).
        val tagSize = synchsafe(bytes, 6)
        if (tagSize <= 0) return emptyList()

        // Дополнительный заголовок (v2.4, флаг 0x40).
        var pos = 10
        if (major >= 4 && (flags and 0x40) != 0) pos += 6

        val end = min(bytes.size, 10 + tagSize)
        val frames = mutableListOf<Frame>()
        while (pos + 10 <= end) {
            if (bytes[pos].toInt() == 0 && bytes[pos + 1].toInt() == 0) break // padding
            val id = decodeAscii(bytes, pos, 4)
            if (id.isBlank()) break
            val size = when (major) {
                3 -> bytes[pos + 4].toLong().let {
                    (it and 0xFF shl 24) or ((bytes[pos + 5].toLong() and 0xFF) shl 16) or
                        ((bytes[pos + 6].toLong() and 0xFF) shl 8) or (bytes[pos + 7].toLong() and 0xFF)
                }.toInt()
                4 -> synchsafe(bytes, pos + 4)
                else -> (bytes[pos + 4].toLong() and 0xFF shl 8 or (bytes[pos + 5].toLong() and 0xFF)).toInt()
            }
            val headerLen = if (major == 2) 6 else 10
            val frameFlagsLen = if (major == 3) 2 else if (major == 4) 2 else 0
            var payloadStart = pos + headerLen + frameFlagsLen
            if (major == 4) {
                // В v2.4 у фреймов может быть доп. заголовок.
                if (payloadStart + 4 <= end) {
                    val fh = synchsafe(bytes, payloadStart)
                    if (fh > 0 && payloadStart + 4 + fh <= end) {
                        payloadStart = payloadStart + 4
                    }
                }
            }
            val payloadSize = if (payloadStart + size <= end) size else end - payloadStart
            if (payloadSize > 0) {
                frames.add(Frame(id, bytes.copyOfRange(payloadStart, payloadStart + payloadSize)))
            }
            pos = payloadStart + size
        }
        return frames
    }

    /** Возвращает декодированный текст первого найденного фрейма из списка id. */
    private fun frameText(frames: List<Frame>, vararg ids: String): String {
        for (id in ids) {
            val f = frames.firstOrNull { it.id == id } ?: continue
            val t = decodeText(f.data)
            if (t.isNotBlank()) return t.trim()
        }
        return ""
    }

    private fun genreText(raw: String): String {
        if (raw.isBlank()) return ""
        // "(\d+)"/"(RX)"/(17) + суффикс
        val m = Regex("^\\((\\d+)\\)(.*)$").find(raw.trim())
        return if (m != null) {
            val num = m.groupValues[1].toIntOrNull()
            val rest = m.groupValues[2].trim()
            val name = num?.let { Genres.getOrNull(it) } ?: ""
            if (rest.isNotEmpty()) rest else name
        } else raw.trim()
    }

    private fun indexOfZero(d: ByteArray, start: Int): Int {
        var i = start
        while (i < d.size) {
            if (d[i].toInt() == 0) return i
            i++
        }
        return -1
    }

    private fun imageFrames(bytes: ByteArray, frames: List<Frame>): List<ByteArray> {
        val result = mutableListOf<ByteArray>()
        for (f in frames) {
            val d = f.data
            if (d.isEmpty()) continue
            if (f.id == "APIC" || f.id == "PIC") {
                val data = when (f.id) {
                    "APIC" -> {
                        val enc = d[0].toInt() and 0xFF
                        // <encoding><mime>\0<type><desc>\0<data>
                        val mimeEnd = indexOfZero(d, 1)
                        if (mimeEnd <= 1) continue
                        var p = mimeEnd + 1
                        p += 1 // type byte
                        val descEnd = if (enc == 1 || enc == 2) {
                            // текст в UTF-16: \0\0
                            findUtf16Null(d, p)
                        } else {
                            indexOfZero(d, p)
                        }
                        if (descEnd < 0) continue
                        p = descEnd + 1
                        if (p >= d.size) continue
                        d.copyOfRange(p, d.size)
                    }
                    else -> {
                        // PIC (v2.2): <enc><format 3><type><desc>\0<data>
                        var p = 7
                        val descEnd = indexOfZero(d, p)
                        if (descEnd < 0) continue
                        p = descEnd + 1
                        if (p >= d.size) continue
                        d.copyOfRange(p, d.size)
                    }
                }
                if (data.isNotEmpty()) result.add(data)
            }
        }
        return result
    }

    private fun findUtf16Null(d: ByteArray, start: Int): Int {
        var i = start
        while (i + 1 < d.size) {
            if (d[i].toInt() == 0 && d[i + 1].toInt() == 0) return i + 1
            i++
        }
        return -1
    }

    /** Декодирует текст согласно ID3v2 «text encoding» байту (0,1,2,3). */
    private fun decodeText(data: ByteArray): String {
        if (data.isEmpty()) return ""
        val enc = data[0].toInt() and 0xFF
        val body = data.copyOfRange(1, data.size)
        return when (enc) {
            0 -> decodeLatin1(body)
            1 -> decodeUtf16(body, bom = true)
            2 -> decodeUtf16(body, bom = false)
            3 -> decodeUtf8(body)
            else -> decodeLatin1(body)
        }
    }

    private fun decodeLatin1(b: ByteArray): String {
        val sb = StringBuilder(b.size)
        for (x in b) {
            if (x.toInt() == 0) break
            sb.append((x.toInt() and 0xFF).toChar())
        }
        return sb.toString()
    }

    private fun decodeUtf8(b: ByteArray): String {
        val len = b.indexOfFirst { it.toInt() == 0 }.let { if (it < 0) b.size else it }
        val sb = StringBuilder(len)
        var i = 0
        while (i < len) {
            val u = b[i].toInt() and 0xFF
            val cp: Int
            when {
                u < 0x80 -> { cp = u; i++ }
                u < 0xC0 -> { cp = 0xFFFD; i++ }
                u < 0xE0 -> {
                    if (i + 1 < len) {
                        cp = ((u and 0x1F) shl 6) or (b[i + 1].toInt() and 0x3F)
                        i += 2
                    } else { cp = 0xFFFD; i++ }
                }
                u < 0xF0 -> {
                    if (i + 2 < len) {
                        cp = ((u and 0x0F) shl 12) or ((b[i + 1].toInt() and 0x3F) shl 6) or (b[i + 2].toInt() and 0x3F)
                        i += 3
                    } else { cp = 0xFFFD; i++ }
                }
                else -> { cp = 0xFFFD; i++ }
            }
            sb.appendCodePoint(cp)
        }
        return sb.toString()
    }

    private fun decodeUtf16(b: ByteArray, bom: Boolean): String {
        val sb = StringBuilder()
        var i = 0
        var bigEndian = true
        if (bom && b.size >= 2) {
            val first = (b[0].toInt() and 0xFF) shl 8 or (b[1].toInt() and 0xFF)
            when (first) {
                0xFEFF -> { bigEndian = true; i = 2 }
                0xFFFE -> { bigEndian = false; i = 2 }
                else -> {}
            }
        }
        var terminated = false
        while (i + 1 < b.size) {
            val lo = b[i].toInt() and 0xFF
            val hi = b[i + 1].toInt() and 0xFF
            val unit = if (bigEndian) (lo shl 8) or hi else (hi shl 8) or lo
            i += 2
            if (unit == 0) { terminated = true; break }
            // Суррогатная пара (не критично для заголовков треков)
            sb.append(unit.toChar())
        }
        return sb.toString()
    }

    private fun decodeAscii(bytes: ByteArray, off: Int, len: Int): String =
        buildString {
            for (i in off until min(off + len, bytes.size)) {
                val c = bytes[i].toInt() and 0xFF
                if (c == 0) break
                append(c.toChar())
            }
        }

    /** Синхробезопасное число (7 полезных бит на байт). */
    private fun synchsafe(bytes: ByteArray, off: Int): Int =
        ((bytes[off].toInt() and 0x7F) shl 21) or
            ((bytes[off + 1].toInt() and 0x7F) shl 14) or
            ((bytes[off + 2].toInt() and 0x7F) shl 7) or
            (bytes[off + 3].toInt() and 0x7F)

    private data class Frame(val id: String, val data: ByteArray)
}

/** Имена жанров ID3 (ID3v1 стандарт, частично расширенный). */
private val Genres: List<String> = listOf(
    "Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge", "Hip-Hop",
    "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B", "Rap", "Reggae", "Rock",
    "Techno", "Industrial", "Alternative", "Ska", "Death Metal", "Pranks", "Soundtrack",
    "Euro-Techno", "Ambient", "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance",
    "Classical", "Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel", "Noise",
    "AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative", "Instrumental Pop",
    "Instrumental Rock", "Ethnic", "Gothic", "Darkwave", "Techno-Industrial", "Electronic",
    "Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40",
    "Christian Rap", "Pop/Funk", "Jungle", "Native American", "Cabaret", "New Wave", "Psychadelic",
    "Rave", "Showtunes", "Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz", "Polka",
    "Retro", "Musical", "Rock & Roll", "Hard Rock",
)
