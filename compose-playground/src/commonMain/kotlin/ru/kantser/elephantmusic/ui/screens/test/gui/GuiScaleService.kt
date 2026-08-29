package ru.kantser.elephantmusic.ui.screens.test.gui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue

/**
 * Сервис масштабирования (Koin). Считает, во сколько раз тестовое поле (GuiGeometry W x H)
 * увеличено/уменьшено, чтобы оно влезло в доступное место окна, — полностью в dp.
 * Параметры настройки (мин/макс/ручной множитель) — изменяемые Compose-состояния,
 * поэтому ползунки на панели отладки меняют их «на лету».
 */
interface GuiScaleService {
    var minScale: Float
    var maxScale: Float
    var manualFactor: Float

    /** Итоговый масштаб по доступной области (dp): min-фит с зажатием в [minScale..maxScale]. */
    fun scaleOf(availWdp: Float, availHdp: Float): Float
}

class GuiScaleServiceImpl(
    minScale: Float = 0.1f,
    maxScale: Float = 4f,
    manualFactor: Float = 1f,
) : GuiScaleService {
    override var minScale by mutableFloatStateOf(minScale)
    override var maxScale by mutableFloatStateOf(maxScale)
    override var manualFactor by mutableFloatStateOf(manualFactor)

    override fun scaleOf(availWdp: Float, availHdp: Float): Float {
        val s = minOf(
            if (availWdp > 0) availWdp / GuiGeometry.W else 1f,
            if (availHdp > 0) availHdp / GuiGeometry.H else 1f,
        ).coerceIn(minScale, maxScale)
        return s * manualFactor
    }
}
