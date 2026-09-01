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
    @Volatile
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

    @Volatile
    private var durationSeconds = 0.0
    private var onReady: (() -> Unit)? = null
    private var onEnd: (() -> Unit)? = null

    override var onLevel: ((List<Float>) -> Unit)? = null

    private companion object {
        const val BAND_COUNT = 20
    }
    private val levels = FloatArray(BAND_COUNT)

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
            val raw = AudioSystem.getAudioInputStream(file)
            val base = raw.format

            // Черновая длительность — из числа кадров или свойства "duration".
            // Внимание: единицы у разных SPI и у разных полей различаются (микро-/миллисекунды,
            // число MP3-кадров против кадров PCM). Поэтому это только грубая прикидка;
            // точное значение даёт фоновый проброс ниже, в тех же единицах, что и позиция.
            val propDur = (fileFormat.getProperty("duration") as? Number)?.toDouble()
            val propSeconds = propDur?.let { if (it >= 1_000_000.0) it / 1_000_000.0 else it / 1_000.0 }
            durationSeconds = propSeconds
                ?: if (fileFormat.frameLength > 0 && fileFormat.format.frameRate > 0f) {
                    fileFormat.frameLength.toDouble() / fileFormat.format.frameRate
                } else if (raw.frameLength > 0 && base.frameRate > 0f) {
                    raw.frameLength.toDouble() / base.frameRate
                } else {
                    0.0
                }

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

            // Точная длительность: декодируем файл в фоне и считаем суммарные PCM-байты.
            // Делим на bytesPerSecond — те же единицы, что и currentPositionSeconds,
            // поэтому прогресс не может «улететь» вперёд ни на одной платформе/формате.
            val probeFile = file
            Thread {
                try {
                    val rawProbe = AudioSystem.getAudioInputStream(probeFile)
                    val probeFmt = format ?: return@Thread
                    val decProbe = AudioSystem.getAudioInputStream(probeFmt, rawProbe)
                    val bps = probeFmt.sampleRate.toDouble() * probeFmt.frameSize
                    var total = 0L
                    val buf = ByteArray(65536)
                    while (true) {
                        val n = decProbe.read(buf)
                        if (n < 0) break
                        total += n
                    }
                    decProbe.close()
                    if (bps > 0 && total > 0 && probeFile === this.file) {
                        durationSeconds = total / bps
                    }
                } catch (_: Exception) {
                }
            }.apply { isDaemon = true; name = "duration-probe"; start() }

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
                computeLevels(buffer, n)
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

    /** Считает RMS-энергии по [BAND_COUNT] полосам из куска PCM и вызывает onLevel. */
    private fun computeLevels(buf: ByteArray, n: Int) {
        val cb = onLevel ?: return
        val samples = n / 2
        if (samples < BAND_COUNT) return
        val per = samples / BAND_COUNT
        var any = false
        for (b in 0 until BAND_COUNT) {
            val start = b * per
            val end = if (b == BAND_COUNT - 1) samples else start + per
            var sum = 0.0
            var i = start
            while (i < end) {
                val lo = buf[i * 2].toInt() and 0xFF
                val hi = buf[i * 2 + 1].toInt()
                val s = (hi shl 8) or lo
                sum += s.toDouble() * s
                i++
            }
            val rms = kotlin.math.sqrt(sum / per) / 32768.0
            val v = rms.coerceIn(0.0, 1.0).toFloat()
            if (v > levels[b]) levels[b] = v
            else levels[b] = (levels[b] * 0.80f).coerceAtMost(v)
            any = true
        }
        if (any) cb(levels.toList())
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
