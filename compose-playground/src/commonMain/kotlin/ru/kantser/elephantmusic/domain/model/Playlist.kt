package ru.kantser.elephantmusic.domain.model

data class Playlist(
    val name: String,
    val tracks: List<Track>,
)
