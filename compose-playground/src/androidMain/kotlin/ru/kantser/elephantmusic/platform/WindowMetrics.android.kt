package ru.kantser.elephantmusic.platform

import android.util.DisplayMetrics
import android.view.WindowManager
import ru.kantser.elephantmusic.AppContextHolder

/** Android-реализация сервиса сведений о дисплее (разрешение + плотность). */
private class AndroidWindowMetricsProvider : WindowMetricsProvider {
    private val log: AppLog = createAppLog()
    private var logged = false

    @Suppress("DEPRECATION")
    private fun displayMetrics(): DisplayMetrics? = try {
        val wm = AppContextHolder.context.getSystemService(android.content.Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        wm.defaultDisplay.getRealMetrics(dm)
        dm
    } catch (e: Exception) {
        null
    }

    private fun toDisplayInfo(dm: DisplayMetrics): DisplayInfo {
        val densityDpi = dm.densityDpi.toFloat()
        val scale = dm.density
        return DisplayInfo(
            id = "DEFAULT_DISPLAY",
            name = "default",
            widthPx = dm.widthPixels,
            heightPx = dm.heightPixels,
            densityDpi = densityDpi,
            scaleFactor = scale,
            isPrimary = true,
        )
    }

    override fun displays(): List<DisplayInfo> {
        val dm = displayMetrics() ?: return emptyList()
        val info = toDisplayInfo(dm)
        if (!logged) {
            log.i(TAG, "display[${info.id}] ${info.name} ${info.widthPx}x${info.heightPx} " +
                "dpi=${info.densityDpi} scale=${info.scaleFactor} primary=${info.isPrimary}")
            logged = true
        }
        return listOf(info)
    }

    override fun primary(): DisplayInfo? = displays().firstOrNull()

    override fun activeDisplay(): DisplayInfo? = primary()
}

private const val TAG = "WindowMetrics"

actual fun createWindowMetricsProvider(): WindowMetricsProvider = AndroidWindowMetricsProvider()
