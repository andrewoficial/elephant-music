package ru.kantser.elephantmusic.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Track(
    val title: String,
    val artist: String,
    val filePath: String,
    val durationSeconds: Int = 0,
    val rating: Int? = null,
    val tags: List<String> = emptyList(),
    val coverArtPath: String? = null,
    val lyrics: String? = null,
)
