package ru.kantser.elephantmusic.platform

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.FloatControl
import javax.sound.sampled.SourceDataLine
import java.io.File
import kotlin.math.min

class DesktopAudioPlayer : AudioPlayer {
    private var file: File? = null
    private var format: AudioFormat? = null
    private var stream: AudioInputStream? = null
    private var line: SourceDataLine? = null
    private var playThread: Thread? = null

    @Volatile
    private var bytesWritten = 0L
    @Volatile
    private var manualStop = false
    @Volatile
    private var playing = false

    private var durationSeconds = 0.0
    private var onReady: (() -> Unit)? = null
    private var onEnd: (() -> Unit)? = null

    private val bytesPerSecond: Double
        get() = format?.let { it.sampleRate.toDouble() * it.frameSize } ?: 0.0

    override fun load(path: String, onReady: () -> Unit, onEnd: () -> Unit): Boolean {
        release()
        this.onReady = onReady
        this.onEnd = onEnd
        return try {
            val file = resolveFile(path)
            if (!file.exists()) {
                System.err.println("AudioPlayer.load: файл не найден [$path]")
                return false
            }
            this.file = file

            val fileFormat = AudioSystem.getAudioFileFormat(file)
            durationSeconds = (fileFormat.getProperty("duration") as? Number)?.toLong()?.div(1_000_000.0)
                ?: (fileFormat.frameLength.toDouble() / fileFormat.format.frameRate)

            val raw = AudioSystem.getAudioInputStream(file)
            val base = raw.format
            format = AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                base.sampleRate,
                16,
                base.channels,
                base.channels * 2,
                base.sampleRate,
                false,
            )
            stream = AudioSystem.getAudioInputStream(format, raw)

            val info = DataLine.Info(SourceDataLine::class.java, format)
            val l = AudioSystem.getLine(info) as SourceDataLine
            l.open(format)
            line = l

            bytesWritten = 0
            manualStop = false
            onReady()
            true
        } catch (e: Exception) {
            System.err.println("AudioPlayer.load error [$path]: $e")
            cleanupLine()
            false
        }
    }

    override fun play() {
        val l = line ?: return
        playing = true
        manualStop = false
        if (!l.isRunning) l.start()
        if (playThread == null || !playThread!!.isAlive) {
            playThread = Thread { pump() }
            playThread?.isDaemon = true
            playThread?.start()
        }
    }

    private fun pump() {
        val l = line ?: return
        val s = stream ?: return
        val buffer = ByteArray(8192)
        try {
            while (!manualStop) {
                val n = s.read(buffer)
                if (n < 0) break
                var off = 0
                while (off < n && !manualStop) {
                    val written = l.write(buffer, off, n - off)
                    off += written
                }
                if (!manualStop) bytesWritten += n
            }
        } catch (_: Exception) {
        }

        if (!manualStop) {
            l.drain()
            l.stop()
            playing = false
            onEnd?.invoke()
        }
    }

    override fun pause() {
        playing = false
        line?.stop()
    }

    override fun stop() {
        manualStop = true
        playing = false
        line?.stop()
        line?.flush()
        reopenAt(0)
        bytesWritten = 0
    }

    override fun release() {
        manualStop = true
        playing = false
        playThread?.interrupt()
        playThread = null
        line?.stop()
        line?.close()
        line = null
        try { stream?.close() } catch (_: Exception) {}
        stream = null
        onReady = null
        onEnd = null
    }

    override fun seekTo(positionSeconds: Double) {
        val bps = bytesPerSecond
        if (bps <= 0) return
        reopenAt((positionSeconds * bps).toLong())
        if (playing) play()
    }

    private fun reopenAt(positionBytes: Long) {
        manualStop = true
        line?.stop()
        line?.flush()
        try { stream?.close() } catch (_: Exception) {}
        try {
            val f = file ?: return
            val raw = AudioSystem.getAudioInputStream(f)
            val s = AudioSystem.getAudioInputStream(format, raw)
            var remaining = positionBytes
            val buf = ByteArray(8192)
            while (remaining > 0) {
                val n = s.read(buf, 0, min(8192, remaining.toInt()))
                if (n < 0) break
                remaining -= n
            }
            stream = s
            bytesWritten = (positionBytes - remaining.coerceAtLeast(0)).coerceAtLeast(0)
        } catch (e: Exception) {
            System.err.println("AudioPlayer.reopenAt error: $e")
        }
        manualStop = false
    }

    override fun currentPositionSeconds(): Double {
        val bps = bytesPerSecond
        return if (bps > 0) bytesWritten / bps else 0.0
    }

    override fun durationSeconds(): Double = durationSeconds

    override fun setVolume(volume: Double) {
        val l = line ?: return
        try {
            val control = l.getControl(FloatControl.Type.MASTER_GAIN)
            if (control is FloatControl) {
                val db = control.minimum + (control.maximum - control.minimum) * volume.coerceIn(0.0, 1.0).toFloat()
                control.value = db
            }
        } catch (_: Exception) {
        }
    }

    private fun cleanupLine() {
        line?.stop()
        line?.close()
        line = null
        try { stream?.close() } catch (_: Exception) {}
        stream = null
    }

    private fun resolveFile(rawPath: String): File {
        var p = rawPath.trim()
        if (p.startsWith("file:", ignoreCase = true)) {
            p = p.removePrefix("file:").removePrefix("//")
        }
        p = decodePercent(p)
        // Windows: срезать лишний ведущий '\' перед буквой диска, напр. "\F:\..."
        if (p.startsWith("\\") && p.length >= 3 && p[1].isLetter() && p[2] == ':') {
            p = p.substring(1)
        }
        p = p.replace('/', File.separatorChar).replace('\\', File.separatorChar)
        return File(p)
    }

    private fun decodePercent(s: String): String {
        if (!s.contains('%')) return s
        val out = StringBuilder(s.length)
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '%' && i + 2 < s.length) {
                val code = s.substring(i + 1, i + 3).toIntOrNull(16)
                if (code != null) {
                    out.append(code.toChar())
                    i += 3
                    continue
                }
            }
            out.append(c)
            i++
        }
        return out.toString()
    }
}

actual fun createAudioPlayer(): AudioPlayer = DesktopAudioPlayer()
