package ru.kantser.elephantmusic.ui.screens.test.gui

import androidx.compose.ui.geometry.Size
import ru.kantser.elephantmusic.platform.AppLog

/**
 * Наблюдатель масштаба поля: принимает вычисленный DeviceScale масштаб и площадь области
 * и отвечает за запись их в GuiDebugState и логирование смены масштаба.
 * Выделен, чтобы DeviceScale (расчёт + раскладка) не смешивал с писать-состояние и логи.
 */
interface ScaleObserver {
    /** Сообщает наблюдателю новый масштаб и площадь области поля (в px, плотность для лога). */
    fun observe(scale: Float, fieldWpx: Float, fieldHpx: Float, density: Float)
}

class ScaleObserverImpl(
    private val debug: GuiDebugState,
    private val log: AppLog,
) : ScaleObserver {
    private var lastLogged = ""

    override fun observe(scale: Float, fieldWpx: Float, fieldHpx: Float, density: Float) {
        debug.appliedScale = scale
        debug.fieldPx = Size(fieldWpx / density, fieldHpx / density)

        val line = "field dp=${(fieldWpx / density).toInt()}x${(fieldHpx / density).toInt()} density=$density scale=$scale"
        if (line != lastLogged) {
            lastLogged = line
            log.d(TAG, line)
        }
    }

    private companion object {
        const val TAG = "TestDevice"
    }
}
