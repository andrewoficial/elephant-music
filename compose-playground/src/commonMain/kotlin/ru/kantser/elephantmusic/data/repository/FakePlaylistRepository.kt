package ru.kantser.elephantmusic.data.repository

import ru.kantser.elephantmusic.domain.model.Playlist
import ru.kantser.elephantmusic.domain.model.Track
import ru.kantser.elephantmusic.domain.repository.PlaylistRepository

class FakePlaylistRepository : PlaylistRepository {
    override fun getPlaylist(): Playlist = Playlist(
        name = "Основной плейлист",
        tracks = listOf(
            Track("Sapphire", "Ed Sheeran", "/music/01.mp3", 179),
            Track("Love Me Not", "Ravyn Lenae", "/music/02.mp3", 213),
            Track("Azizam", "Ed Sheeran", "/music/12.mp3", 122),
            Track("Still Bad", "Lizzo", "/music/16.mp3", 208),
        ),
    )
}
