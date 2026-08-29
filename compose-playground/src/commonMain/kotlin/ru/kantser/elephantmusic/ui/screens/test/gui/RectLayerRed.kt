package ru.kantser.elephantmusic.ui.screens.test.gui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Красный прямоугольник: заполняет всё логическое поле GuiGeometry.Red (0,0,300,300).
 */
object RectLayerRed {
    @Composable
    fun View(modifier: Modifier = Modifier) {
        Canvas(modifier) { drawInDeviceSpace { draw() } }
    }

    fun DrawScope.draw() {
        val g = GuiGeometry.Red
        drawRect(Color(0xFFe53935), topLeft = g.topLeft, size = g.size)
    }
}
