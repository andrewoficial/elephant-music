package ru.kantser.elephantmusic.data.repository

import ru.kantser.elephantmusic.domain.model.Playlist
import ru.kantser.elephantmusic.domain.repository.PlaylistRepository

class PlaylistRepositoryImpl(private val store: JsonFileStore) : PlaylistRepository {
    override fun load(): List<Playlist> = store.read("playlist.json") ?: emptyList()

    override fun save(playlists: List<Playlist>) = store.write("playlist.json", playlists)

    override fun defaultPlaylist(): Playlist = Playlist(name = "Основной плейлист")
}
