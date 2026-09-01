package ru.kantser.elephantmusic.platform

expect suspend fun pickAudioFiles(): List<String>

/**
 * Выбирает папку с аудио и возвращает список найденных в ней аудиофайлов
 * (абсолютные пути на desktop, content:// URI на android). Пусто — отмена/нет файлов.
 */
expect suspend fun pickAudioFolder(): List<String>
