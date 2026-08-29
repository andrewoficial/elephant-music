package ru.kantser.elephantmusic.platform

import java.awt.GraphicsEnvironment
import java.awt.Toolkit

/** AWT-реализация сервиса сведений о мониторах (разрешение + плотность + масштаб ОС). */
private class AwtWindowMetricsProvider : WindowMetricsProvider {
    private val log: AppLog = createAppLog()
    private var logged = false

    private fun toDisplayInfo(index: Int, device: java.awt.GraphicsDevice): DisplayInfo {
        val cfg = device.defaultConfiguration
        val bounds = cfg.bounds
        val scaleFactor = try {
            Toolkit.getDefaultToolkit().screenResolution / 96f
        } catch (e: Exception) {
            1f
        }
        val densityDpi = scaleFactor * 96f
        return DisplayInfo(
            id = "DISPLAY${index + 1}",
            name = "DISPLAY${index + 1}",
            widthPx = bounds.width,
            heightPx = bounds.height,
            densityDpi = densityDpi,
            scaleFactor = scaleFactor,
            isPrimary = device == GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice,
        )
    }

    override fun displays(): List<DisplayInfo> {
        val devices = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices
        val list = devices.mapIndexed { i, d -> toDisplayInfo(i, d) }
        if (!logged) {
            list.forEach {
                log.i(TAG, "display[${it.id}] ${it.name} ${it.widthPx}x${it.heightPx} " +
                    "dpi=${it.densityDpi} scale=${it.scaleFactor} primary=${it.isPrimary}")
            }
            logged = true
        }
        return list
    }

    override fun primary(): DisplayInfo? =
        displays().firstOrNull { it.isPrimary }

    override fun activeDisplay(): DisplayInfo? {
        // Дисплей под курсором — эвристика активного без ссылки на само окно.
        val pointer = try {
            java.awt.MouseInfo.getPointerInfo()
        } catch (e: Exception) {
            null
        }
        val dev = pointer?.device ?: return primary()
        // Возвращаем по индексу среди screenDevices (id в AWT — это индекс).
        val idx = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices.indexOfFirst { it == dev }
        if (idx >= 0) return displays().getOrNull(idx)
        return primary()
    }
}

private const val TAG = "WindowMetrics"

actual fun createWindowMetricsProvider(): WindowMetricsProvider = AwtWindowMetricsProvider()
