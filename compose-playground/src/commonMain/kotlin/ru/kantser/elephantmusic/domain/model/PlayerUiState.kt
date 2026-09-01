package ru.kantser.elephantmusic.domain.model

/** Всё состояние плеера, наблюдаемое из UI (вынесено из контроллера). */
data class PlayerUiState(
    val playlists: List<Playlist> = emptyList(),
    val currentPlaylistName: String = "",
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val positionSeconds: Double = 0.0,
    val durationSeconds: Double = 0.0,
    val volume: Double = 0.8,
    /** Нормализованные (0..1) уровни спектра по полосам — для визуализатора. */
    val levels: List<Float> = emptyList(),
)
