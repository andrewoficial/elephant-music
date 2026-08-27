package ru.kantser.elephantmusic.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Playlist(
    val name: String,
    val tracks: List<Track> = emptyList(),
)
