package ru.kantser.elephantmusic.ui.screens.test.gui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform

/**
 * Аналог DeviceCanvas.drawInDeviceSpace у Ritmix: растягивает логическое поле
 * GuiGeometry (W x H) на всю площадь Canvas, чтобы координаты из формул
 * математически совпадали с фактической отрисовкой при любой плотности (px == dp).
 */
fun DrawScope.drawInDeviceSpace(content: DrawScope.() -> Unit) {
    val sx = if (size.width > 0f) size.width / GuiGeometry.W else 1f
    val sy = if (size.height > 0f) size.height / GuiGeometry.H else 1f
    withTransform({
        scale(sx, sy, pivot = Offset.Zero)
    }) {
        content()
    }
}
