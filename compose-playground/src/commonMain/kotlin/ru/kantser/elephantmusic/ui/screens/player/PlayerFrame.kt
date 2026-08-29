package ru.kantser.elephantmusic.ui.screens.player

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.kantser.elephantmusic.ui.screens.player.SvgGradients as G
import ru.kantser.elephantmusic.ui.screens.player.SvgGeometry as Geo

/**
 * Рамка дисплея (элемент displayFrame SVG).
 * Контент внутри рамки рисуется отдельно (см. ScreenSurface / меню в Compose).
 */
object PlayerFrame {
    @Composable
    fun View(modifier: Modifier = androidx.compose.ui.Modifier) {
        Canvas(modifier) { drawInDeviceSpace { draw() } }
    }

    fun androidx.compose.ui.graphics.drawscope.DrawScope.draw() {
        val f = Geo.DisplayFrame
        drawRoundRect(G.FrameFill, f.topLeft, f.size, f.radius)
        drawRoundRect(color = G.FrameStroke, topLeft = f.topLeft, size = f.size, cornerRadius = f.radius)
    }
}
