package ru.kantser.elephantmusic.ui.screens.test.gui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

/**
 * Единая карта координат тестового поля (SVG-отладка).
 * Поле — логический квадрат W x H (как viewBox 0 0 300 300), масштабируется
 * на весь Canvas через drawInDeviceSpace (см. TestScaleView).
 *
 * Правило: правим числа здесь — поле, вписанный квадрат и половинки
 * пересчитываются формулами автоматически.
 */
object GuiGeometry {
    // Логический размер поля (аналог SvgGeometry.W/H у Ritmix)
    const val W = 300f
    const val H = 300f

    // Вписанный (синий) квадрат: 200x200 по центру — формула от поля.
    const val InnerSize = 200f

    /** Центрирующий отступ: (поле - фигура)/2. */
    val InnerEdge: Float get() = (W - InnerSize) / 2f

    val Red = RectGeom(0f, 0f, W, H)
    val Blue = RectGeom(InnerEdge, InnerEdge, InnerSize, InnerSize)

    // Белый прямоугольник: новые координаты — угол(100,100), противолежащий (150,200):
    // x от 100 до 150 (ширина 50), y от 100 до 200 (высота 100).
    val White = RectGeom(100f, 100f, 50f, 100f)
}

/** Прямоугольная спецификация (аналог RectGeom у Ritmix): позиция + размер + скругление. */
data class RectGeom(val x: Float, val y: Float, val w: Float, val h: Float, val rx: Float = 0f) {
    val topLeft: Offset get() = Offset(x, y)
    val size: Size get() = Size(w, h)
    val radius: CornerRadius get() = CornerRadius(rx)

    /** Четыре угла по часовой стрелке от левого верхнего. */
    val corners: List<Offset>
        get() = listOf(
            Offset(x, y),
            Offset(x + w, y),
            Offset(x + w, y + h),
            Offset(x, y + h),
        )
}
