package ru.kantser.elephantmusic.domain.repository

import ru.kantser.elephantmusic.domain.model.Playlist

interface PlaylistRepository {
    fun load(): List<Playlist>
    fun save(playlists: List<Playlist>)
    fun defaultPlaylist(): Playlist
}
