package ru.kantser.elephantmusic.ui.screens.test.gui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Белый прямоугольник: левая половина синего (100x100).
 * Координаты — из GuiGeometry.White (формула: середина поля, половина синего).
 */
object RectLayerWhite {
    @Composable
    fun View(modifier: Modifier = Modifier) {
        Canvas(modifier) { drawInDeviceSpace { draw() } }
    }

    fun DrawScope.draw() {
        val g = GuiGeometry.White
        drawRect(Color.White, topLeft = g.topLeft, size = g.size)
    }
}
