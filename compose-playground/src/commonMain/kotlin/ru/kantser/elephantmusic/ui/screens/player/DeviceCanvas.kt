package ru.kantser.elephantmusic.ui.screens.player

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform

/**
 * Canvas-слои прибора рисуются в SVG-координатах viewBox (0..W, 0..H),
 * а Compose-слой (ScreenSurface/хит-зоны) позиционируется в dp.
 * На десктопе density = 1.0 и px == dp, поэтому всё совпадало; на Android
 * density > 1 и px != dp — слои "разъезжались".
 *
 * Этот хелпер растягивает логическое поле WxH на всю площадь Canvas,
 * так что координаты SVG математически совпадают с dp-раскладкой при любой плотности.
 */
fun DrawScope.drawInDeviceSpace(content: DrawScope.() -> Unit) {
    val sx = if (size.width > 0f) size.width / SvgGeometry.W else 1f
    val sy = if (size.height > 0f) size.height / SvgGeometry.H else 1f
    withTransform({
        scale(sx, sy, pivot = Offset.Zero)
    }) {
        content()
    }
}
