package ru.kantser.elephantmusic.platform

import android.media.MediaPlayer

class AndroidAudioPlayer : AudioPlayer {
    private var player: MediaPlayer? = null
    private var onEnd: (() -> Unit)? = null
    private var onReady: (() -> Unit)? = null

    override fun load(path: String, onReady: () -> Unit, onEnd: () -> Unit): Boolean {
        release()
        this.onReady = onReady
        this.onEnd = onEnd
        return try {
            val p = MediaPlayer()
            p.setDataSource(path)
            p.setOnPreparedListener { mp -> this.onReady?.invoke() }
            p.setOnCompletionListener { this.onEnd?.invoke() }
            p.prepare()
            player = p
            onReady()
            true
        } catch (e: Exception) {
            player?.release()
            player = null
            false
        }
    }

    override fun play() {
        player?.start()
    }

    override fun pause() {
        player?.pause()
    }

    override fun stop() {
        player?.pause()
        player?.seekTo(0)
    }

    override fun release() {
        player?.release()
        player = null
        onEnd = null
        onReady = null
    }

    override fun seekTo(positionSeconds: Double) {
        player?.seekTo((positionSeconds * 1000).toInt())
    }

    override fun currentPositionSeconds(): Double =
        player?.currentPosition?.div(1000.0) ?: 0.0

    override fun durationSeconds(): Double =
        player?.duration?.div(1000.0) ?: 0.0

    override fun setVolume(volume: Double) {
        val v = volume.coerceIn(0.0, 1.0).toFloat()
        player?.setVolume(v, v)
    }
}

actual fun createAudioPlayer(): AudioPlayer = AndroidAudioPlayer()
