package ru.kantser.elephantmusic.domain.model

data class Track(
    val title: String,
    val artist: String,
    val filePath: String,
    val durationSeconds: Int,
)
