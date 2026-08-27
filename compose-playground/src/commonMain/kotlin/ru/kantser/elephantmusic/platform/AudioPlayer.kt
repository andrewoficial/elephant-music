package ru.kantser.elephantmusic.platform

interface AudioPlayer {
    fun load(path: String, onReady: () -> Unit, onEnd: () -> Unit): Boolean
    fun play()
    fun pause()
    fun stop()
    fun release()
    fun seekTo(positionSeconds: Double)
    fun currentPositionSeconds(): Double
    fun durationSeconds(): Double
    fun setVolume(volume: Double)
}

expect fun createAudioPlayer(): AudioPlayer
