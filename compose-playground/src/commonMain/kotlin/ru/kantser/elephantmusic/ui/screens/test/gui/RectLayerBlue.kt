package ru.kantser.elephantmusic.ui.screens.test.gui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Синий прямоугольник: вписан в поле по центру (200x200).
 * Координаты берутся из GuiGeometry.Blue (формула от размера поля).
 */
object RectLayerBlue {
    @Composable
    fun View(modifier: Modifier = Modifier) {
        Canvas(modifier) { drawInDeviceSpace { draw() } }
    }

    fun DrawScope.draw() {
        val g = GuiGeometry.Blue
        drawRect(Color(0xFF1e88e5), topLeft = g.topLeft, size = g.size)
    }
}
