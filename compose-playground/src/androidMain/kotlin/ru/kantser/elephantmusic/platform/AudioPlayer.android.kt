package ru.kantser.elephantmusic.platform

import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.net.Uri
import ru.kantser.elephantmusic.AppContextHolder

class AndroidAudioPlayer : AudioPlayer {
    private var player: MediaPlayer? = null
    private var onEnd: (() -> Unit)? = null
    private var onReady: (() -> Unit)? = null
    private var visualizer: Visualizer? = null

    override var onLevel: ((List<Float>) -> Unit)? = null

    private companion object {
        const val BAND_COUNT = 20
    }
    private val levels = FloatArray(BAND_COUNT)

    override fun load(path: String, onReady: () -> Unit, onEnd: () -> Unit): Boolean {
        release()
        this.onReady = onReady
        this.onEnd = onEnd
        return try {
            val p = MediaPlayer()
            if (path.startsWith("content://")) {
                p.setDataSource(AppContextHolder.context, Uri.parse(path))
            } else {
                p.setDataSource(path)
            }
            p.setOnPreparedListener { mp -> this.onReady?.invoke() }
            p.setOnCompletionListener { this.onEnd?.invoke() }
            p.prepare()
            player = p
            attachVisualizer(p.audioSessionId)
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
        detachVisualizer()
        player?.release()
        player = null
        onEnd = null
        onReady = null
    }

    private fun attachVisualizer(sessionId: Int) {
        detachVisualizer()
        if (onLevel == null) return
        try {
            val v = Visualizer(sessionId)
            v.setCaptureSize(Visualizer.getCaptureSizeRange()[1])
            val listener = object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(p0: Visualizer, p1: ByteArray, p2: Int) {
                    computeLevels(p1)
                }

                override fun onFftDataCapture(p0: Visualizer, p1: ByteArray, p2: Int) {}
            }
            v.setDataCaptureListener(listener, Visualizer.getMaxCaptureRate() / 2, true, false)
            v.enabled = true
            visualizer = v
        } catch (e: Exception) {
            visualizer = null
        }
    }

    private fun detachVisualizer() {
        try {
            visualizer?.enabled = false
            visualizer?.release()
        } catch (_: Exception) {
        }
        visualizer = null
    }

    /** Считает RMS-энергии по [BAND_COUNT] полосам из waveform-массива (8-бит 0..255). */
    private fun computeLevels(d: ByteArray) {
        val cb = onLevel ?: return
        val n = d.size
        if (n < BAND_COUNT) return
        val per = n / BAND_COUNT
        var any = false
        for (b in 0 until BAND_COUNT) {
            val start = b * per
            val end = if (b == BAND_COUNT - 1) n else start + per
            var sum = 0.0
            var j = start
            while (j < end) {
                val v = d[j].toInt() and 0xFF
                sum += v * v
                j++
            }
            val rms = kotlin.math.sqrt(sum / per) / 255.0
            val level = rms.coerceIn(0.0, 1.0).toFloat()
            if (level > levels[b]) levels[b] = level
            else levels[b] = (levels[b] * 0.80f).coerceAtMost(level)
            any = true
        }
        if (any) cb(levels.toList())
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
