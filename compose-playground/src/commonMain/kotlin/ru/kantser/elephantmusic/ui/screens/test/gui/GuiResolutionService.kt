package ru.kantser.elephantmusic.ui.screens.test.gui

import ru.kantser.elephantmusic.platform.DisplayInfo
import ru.kantser.elephantmusic.platform.WindowMetricsProvider
import androidx.compose.ui.unit.Density

/**
 * Сервис разрешения (Koin). Отдаёт реальные разрешения мониторов/экрана в px и dp —
 * для отладки и ручной подгонки геометрии. Правишь параметры/реализацию здесь.
 */
interface GuiResolutionService {
    /** Активный дисплей (где окно) — физические пиксели. */
    fun activeDisplay(): DisplayInfo?

    /** Основной дисплей. */
    fun primaryDisplay(): DisplayInfo?

    /** Активный дисплей, пересчитанный в логические dp при заданной плотности. */
    fun activeDisplayDp(density: Density): Pair<Float, Float>?
}

class GuiResolutionServiceImpl(
    private val metrics: WindowMetricsProvider,
) : GuiResolutionService {
    override fun activeDisplay(): DisplayInfo? = metrics.activeDisplay()

    override fun primaryDisplay(): DisplayInfo? = metrics.primary()

    override fun activeDisplayDp(density: Density): Pair<Float, Float>? {
        val d = metrics.activeDisplay() ?: return null
        val w = with(density) { d.widthPx.toDp().value }
        val h = with(density) { d.heightPx.toDp().value }
        return w to h
    }
}
