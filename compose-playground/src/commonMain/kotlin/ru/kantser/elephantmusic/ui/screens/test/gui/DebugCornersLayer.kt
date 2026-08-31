package ru.kantser.elephantmusic.ui.screens.test.gui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import ru.kantser.elephantmusic.ui.screens.test.gui.GuiGeometry as Geo

/**
 * Слой отладки: рисует/скрывает маркеры углов прямоугольников (по формуле из GuiGeometry).
 */
object DebugCornersLayer {
    @Composable
    fun View(show: Boolean, modifier: Modifier = Modifier) {
        if (!show) return
        Canvas(modifier) { drawInDeviceSpace { draw() } }
    }

    fun DrawScope.draw() {
        val targets = listOf(
            Color.White to Geo.Red,
            Color.Yellow to Geo.Blue,
            Color.Black to Geo.White,
        )
        targets.forEach { (dot, g) ->
            g.corners.forEach { c ->
                drawCircle(Color(0xFF00E5FF), radius = 3f, center = c, style = Stroke(1f))
                drawCircle(dot, radius = 2f, center = c)
            }
        }
    }
}
