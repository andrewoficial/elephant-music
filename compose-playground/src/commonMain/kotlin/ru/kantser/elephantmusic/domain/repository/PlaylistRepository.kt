package ru.kantser.elephantmusic.domain.repository

import ru.kantser.elephantmusic.domain.model.Playlist

interface PlaylistRepository {
    fun getPlaylist(): Playlist
}
