package ru.kantser.elephantmusic.ui.screens.test.gui

import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Слой прибора: самостоятельная отрисовка в координатах устройства (внутри letterbox-трансформа).
 * RitmixBodyLayer оркестрирует слои списком, не зная их внутреннее устройство (OCP):
 * добавить кнопки = добавить новый слой в список, не меняя сам оркестратор.
 */
interface DeviceLayer {
    fun draw(scope: DrawScope)
}
