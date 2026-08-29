package ru.kantser.elephantmusic.ui.screens.test.gui

import ru.kantser.elephantmusic.platform.WindowMetricsProvider
import androidx.compose.ui.unit.Density

/**
 * Сервис плотности (Koin). Отдаёт DPI/плотность текущего экрана и Compose-плотность —
 * чтобы видеть, в каких единицах реально рисуется тестовое поле (px vs dp).
 */
interface GuiDpiService {
    /** Плотность активного дисплея (densityDpi). */
    fun activeDisplayDpi(): Float?

    /** Compose-плотность окна (density), переданная из UI. */
    fun densityFactor(density: Density): Float
}

class GuiDpiServiceImpl(
    private val metrics: WindowMetricsProvider,
) : GuiDpiService {
    override fun activeDisplayDpi(): Float? = metrics.activeDisplay()?.densityDpi

    override fun densityFactor(density: Density): Float = density.density
}
