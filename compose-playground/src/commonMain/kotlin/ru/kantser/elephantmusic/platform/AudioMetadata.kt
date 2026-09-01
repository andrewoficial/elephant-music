package ru.kantser.elephantmusic.platform

/**
 * Считанные из аудиофайла ID3-теги (и близкое к ним). Поля-строки могут быть пустыми,
 * если тег отсутствует или не прочитался.
 */
data class AudioMetadata(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val year: String = "",
    val trackNumber: String = "",
    val genre: String = "",
    val composer: String = "",
    val lyrics: String? = null,
    /** Путь к извлечённой обложке (кэш-файл), либо null. */
    val coverArtPath: String? = null,
)

/** Прочитать ID3-теги из аудиофайла. [path] — путь к файлу или content:// URI. */
expect fun readAudioMetadata(path: String): AudioMetadata
