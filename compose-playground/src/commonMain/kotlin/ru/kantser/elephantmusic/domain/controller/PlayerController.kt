package ru.kantser.elephantmusic.domain.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.kantser.elephantmusic.data.service.LastFmService
import ru.kantser.elephantmusic.domain.model.PlayerUiState
import ru.kantser.elephantmusic.domain.model.Playlist
import ru.kantser.elephantmusic.domain.model.PlayerState
import ru.kantser.elephantmusic.domain.model.Track
import ru.kantser.elephantmusic.domain.repository.PlaylistRepository
import ru.kantser.elephantmusic.domain.repository.PlayerStateRepository
import ru.kantser.elephantmusic.domain.repository.SettingsRepository
import ru.kantser.elephantmusic.platform.AudioPlayer
import kotlin.math.absoluteValue

/**
 * Контроллер воспроизведения: владеет общим состоянием плеера и логикой работы с аудио
 * (play/pause/next/previous/seek/tick). Управление плейлистами/треками — в расширениях
 * (см. PlayerPlaylistOps.kt), чтобы файл не разрастался.
 */
class PlayerController(
    internal val playlistRepository: PlaylistRepository,
    private val playerStateRepository: PlayerStateRepository,
    private val settingsRepository: SettingsRepository,
    internal val audioPlayer: AudioPlayer,
    private val lastFmService: LastFmService,
) {
    var state by mutableStateOf(PlayerUiState())
        internal set

    private var currentIndex = -1
    private var loadedPath: String? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        val settings = settingsRepository.load()
        val playlists = playlistRepository.load().ifEmpty {
            listOf(playlistRepository.defaultPlaylist())
        }
        val currentPlaylist = playlists.first()
        var currentTrack: Track? = null

        val saved = playerStateRepository.load()
        if (saved?.trackFilePath != null) {
            val idx = currentPlaylist.tracks.indexOfFirst { it.filePath == saved.trackFilePath }
            if (idx >= 0) {
                currentIndex = idx
                currentTrack = currentPlaylist.tracks[idx]
            }
        }

        state = state.copy(
            playlists = playlists,
            currentPlaylistName = currentPlaylist.name,
            currentTrack = currentTrack,
            volume = settings.volume,
        )
        audioPlayer.setVolume(settings.volume)
    }

    private fun currentPlaylist(): Playlist =
        state.playlists.firstOrNull { it.name == state.currentPlaylistName } ?: state.playlists.first()

    fun playPause() {
        val track = state.currentTrack ?: return
        if (state.isPlaying) {
            audioPlayer.pause()
            state = state.copy(isPlaying = false, positionSeconds = audioPlayer.currentPositionSeconds())
            savePosition()
        } else {
            if (ensureLoaded(track)) {
                audioPlayer.play()
                state = state.copy(isPlaying = true)
            }
        }
    }

    fun playTrack(track: Track) {
        val playlist = currentPlaylist()
        currentIndex = playlist.tracks.indexOf(track)
        if (!ensureLoaded(track)) return
        audioPlayer.play()
        state = state.copy(
            currentTrack = track,
            isPlaying = true,
            positionSeconds = 0.0,
            durationSeconds = audioPlayer.durationSeconds(),
        )
        nowPlaying(track)
    }

    fun next() {
        val tracks = currentPlaylist().tracks
        if (tracks.isEmpty()) return
        currentIndex = (currentIndex + 1) % tracks.size
        playAt(currentIndex)
    }

    fun previous() {
        val tracks = currentPlaylist().tracks
        if (tracks.isEmpty()) return
        currentIndex = if (currentIndex <= 0) tracks.size - 1 else currentIndex - 1
        playAt(currentIndex)
    }

    fun stop() {
        audioPlayer.stop()
        state = state.copy(isPlaying = false, positionSeconds = 0.0)
        savePosition()
    }

    fun seek(percent: Double) {
        val duration = state.durationSeconds
        if (duration <= 0) return
        audioPlayer.seekTo(duration * percent.coerceIn(0.0, 100.0) / 100.0)
        state = state.copy(positionSeconds = audioPlayer.currentPositionSeconds())
    }

    fun setVolume(volume: Double) {
        val v = volume.coerceIn(0.0, 1.0)
        state = state.copy(volume = v)
        audioPlayer.setVolume(v)
        settingsRepository.save(settingsRepository.load().copy(volume = v))
    }

    fun tick() {
        if (!state.isPlaying) return
        val pos = audioPlayer.currentPositionSeconds()
        if ((pos - state.positionSeconds).absoluteValue >= 0.5) {
            state = state.copy(
                positionSeconds = pos,
                durationSeconds = audioPlayer.durationSeconds(),
            )
        }
    }

    internal fun savePlaylists(playlists: List<Playlist>) {
        state = state.copy(playlists = playlists)
        playlistRepository.save(playlists)
    }

    internal fun pathToTrack(path: String): Track {
        val fileName = path.substringAfterLast('/').substringAfterLast('\\')
        val name = fileName.substringBeforeLast('.')
        return Track(title = name, artist = "Unknown", filePath = path)
    }

    private fun playAt(index: Int) {
        val track = currentPlaylist().tracks[index]
        if (!ensureLoaded(track)) return
        audioPlayer.play()
        state = state.copy(
            currentTrack = track,
            isPlaying = true,
            positionSeconds = 0.0,
            durationSeconds = audioPlayer.durationSeconds(),
        )
        nowPlaying(track)
    }

    private fun ensureLoaded(track: Track): Boolean {
        if (loadedPath != track.filePath) {
            val ok = audioPlayer.load(
                track.filePath,
                onReady = {
                    state = state.copy(durationSeconds = audioPlayer.durationSeconds())
                },
                onEnd = { onTrackEnded(track) },
            )
            if (!ok) {
                loadedPath = null
                state = state.copy(isPlaying = false)
                return false
            }
            loadedPath = track.filePath
        }
        audioPlayer.setVolume(state.volume)
        return true
    }

    private fun onTrackEnded(track: Track) {
        val played = audioPlayer.currentPositionSeconds()
        scope.launch {
            lastFmService.scrobble(track, played, state.durationSeconds)
        }
        state = state.copy(isPlaying = false)
        savePosition()
        next()
    }

    private fun nowPlaying(track: Track) {
        scope.launch {
            lastFmService.updateNowPlaying(track)
        }
    }

    private fun savePosition() {
        val track = state.currentTrack ?: return
        val position = audioPlayer.currentPositionSeconds()
        playerStateRepository.save(
            PlayerState(
                playlistName = state.currentPlaylistName,
                trackFilePath = track.filePath,
                positionSeconds = position,
            )
        )
    }
}
