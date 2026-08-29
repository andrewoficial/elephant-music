package ru.kantser.elephantmusic.domain.controller

import ru.kantser.elephantmusic.domain.model.Playlist
import ru.kantser.elephantmusic.domain.model.Track
import ru.kantser.elephantmusic.platform.deleteFileCompletely

/**
 * Управление плейлистами и треками: расширения PlayerController (вынесены из самого
 * класса, чтобы последний не разрастался). Читают/обновляют общий state контроллера.
 */
fun PlayerController.addTracks(paths: List<String>) {
    addTracksToCurrentPlaylist(paths)
}

fun PlayerController.addTracksToCurrentPlaylist(paths: List<String>) {
    if (paths.isEmpty()) return
    val tracks = paths.map { pathToTrack(it) }
    savePlaylists(
        state.playlists.map { pl ->
            if (pl.name == state.currentPlaylistName) pl.copy(tracks = pl.tracks + tracks) else pl
        },
    )
}

fun PlayerController.addTracksToPlaylist(paths: List<String>, playlistName: String) {
    if (paths.isEmpty()) return
    val tracks = paths.map { pathToTrack(it) }
    savePlaylists(
        state.playlists.map { pl ->
            if (pl.name == playlistName) pl.copy(tracks = pl.tracks + tracks) else pl
        },
    )
}

fun PlayerController.createPlaylist(name: String): Boolean {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return false
    if (state.playlists.any { it.name == trimmed }) return false
    val new = Playlist(name = trimmed)
    savePlaylists(state.playlists + new)
    state = state.copy(currentPlaylistName = trimmed)
    return true
}

fun PlayerController.selectPlaylist(name: String) {
    if (state.playlists.any { it.name == name }) {
        state = state.copy(currentPlaylistName = name)
    }
}

fun PlayerController.renamePlaylist(oldName: String, newName: String) {
    val target = newName.trim()
    if (target.isEmpty()) return
    if (state.playlists.any { it.name == oldName } && state.playlists.none { it.name == target }) {
        val updated = state.playlists.map { pl ->
            if (pl.name == oldName) pl.copy(name = target) else pl
        }
        savePlaylists(updated)
        state = state.copy(
            currentPlaylistName = if (state.currentPlaylistName == oldName) target else state.currentPlaylistName,
        )
    }
}

fun PlayerController.deletePlaylist(name: String) {
    if (state.playlists.size <= 1) return
    if (state.playlists.none { it.name == name }) return
    val remaining = state.playlists.filterNot { it.name == name }
    savePlaylists(remaining)
    if (state.currentPlaylistName == name) {
        state = state.copy(currentPlaylistName = remaining.first().name)
    }
}

fun PlayerController.removeTrackFromCurrent(track: Track) {
    removeTrack(track)
}

private fun PlayerController.removeTrack(track: Track) {
    savePlaylists(
        state.playlists.map { pl ->
            if (pl.name == state.currentPlaylistName) {
                pl.copy(tracks = pl.tracks.filterNot { it.filePath == track.filePath })
            } else pl
        },
    )
}

fun PlayerController.moveTrack(track: Track, targetPlaylistName: String) {
    if (targetPlaylistName == state.currentPlaylistName) return
    val target = state.playlists.firstOrNull { it.name == targetPlaylistName } ?: return
    if (target.tracks.any { it.filePath == track.filePath }) return
    val updated = state.playlists.map { pl ->
        when (pl.name) {
            state.currentPlaylistName -> pl.copy(tracks = pl.tracks.filterNot { it.filePath == track.filePath })
            targetPlaylistName -> pl.copy(tracks = pl.tracks + track)
            else -> pl
        }
    }
    savePlaylists(updated)
}

fun PlayerController.copyTrack(track: Track, targetPlaylistName: String) {
    if (targetPlaylistName == state.currentPlaylistName) return
    val target = state.playlists.firstOrNull { it.name == targetPlaylistName } ?: return
    if (target.tracks.any { it.filePath == track.filePath }) return
    savePlaylists(
        state.playlists.map { pl ->
            if (pl.name == targetPlaylistName) pl.copy(tracks = pl.tracks + track) else pl
        },
    )
}

fun PlayerController.deleteFileAndRemove(track: Track): Boolean {
    val wasCurrent = state.currentTrack?.filePath == track.filePath
    if (wasCurrent) {
        audioPlayer.stop()
        state = state.copy(isPlaying = false, currentTrack = null, positionSeconds = 0.0)
    }
    removeTrack(track)
    return deleteFileCompletely(track.filePath)
}
