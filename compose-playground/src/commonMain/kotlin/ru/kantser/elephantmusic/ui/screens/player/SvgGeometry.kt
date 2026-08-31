package ru.kantser.elephantmusic.ui.screens.player

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

/**
 * Единая карта координат упрощённого SVG (RITMIX RF-8800).
 * Имя = id элемента в SVG. Правка числа здесь = правка в SVG (viewBox 0 0 520 350).
 *
 * Показывает, где именно (в каких координатах устройства) лежит каждый элемент.
 */
object SvgGeometry {
    const val W = 520f
    const val H = 350f

    // ===== Корпус =====
    val BodyShell = RectGeom(15f, 45f, 470f, 250f, 30f)
    val BodyEdge = RectGeom(17f, 47f, 466f, 246f, 28f)
    val BodyFace = RectGeom(22f, 52f, 456f, 236f, 24f)
    val BodyGloss = BodyFace

    // ===== Логотип: transform="translate(10.79 238.92) rotate(-90) scale(0.17)" =====
    object Logo {
        const val Tx = 10.79f
        const val Ty = 238.92f
        const val RotationDeg = -90f
        const val Scale = 0.17f
    }

    // ===== Рамка дисплея (контент рисуется в Compose) =====
    val DisplayFrame = RectGeom(70f, 76f, 332f, 196f, 8f)

    // ===== Кнопочный брусок (4 кнопки по 18x38, шаг 40) =====
    object Buttons {
        const val BodyX = 442f
        const val BodyY0 = 89f
        const val BodyW = 18f
        const val BodyH = 38f
        const val Step = 40f
        fun bodyY(index: Int): Float = BodyY0 + index * Step

        const val FaceX = 445.5f
        const val FaceW = 11f
        const val FaceH = 36f

        // Шелкография (иконки) на корпусе, x 410–428
        const val IconX = 410f
        const val IconColor = "eef2f5"
        object Icons {
            const val RewindY = 102f
            const val MenuY = 152.5f
            const val MenuX = 418.9f
            const val PlayY = 182f
            const val FwdY = 228f

            // Охватывающие боксы значков (шеколография), в координатах устройства.
            // Позиции/размеры выведены из реальной отрисовки (ButtonRail.drawGraphicIcons).
            val RewindBox = RectGeom(IconX, RewindY, 18f, 12f, 0f)
            val PlayBox = RectGeom(IconX, PlayY, 18f, 12f, 0f)
            val FwdBox = RectGeom(IconX + 7f, FwdY - 6f, 11f, 18f, 0f)
            val MenuTopLeft = Offset(MenuX - 6f, MenuY - 14f)
        }

        // Невидимые зоны клика (иконка + зазор + кнопка = одно нажатие)
        object HitZones {
            const val x = 406f
            const val y = 89f
            const val w = 56f
            const val h = 38f
        }
    }

    // ===== Экран (контент) в ЛОКАЛЬНЫХ координатах рамки дисплея =====
    // Рамка (displayFrame) занимает (0,0,332,196); контент — фрейм внутри с полями 6.
    object Screen {
        const val LocalW = 332f
        const val LocalH = 196f

        const val LightX = 6f
        const val LightY = 6f
        const val LightW = 202f
        const val LightH = 184f

        const val PanelX = 208f
        const val PanelY = 6f
        const val PanelW = 118f
        const val PanelH = 184f

        const val TopBarH = 16f

        // Пункты главного меню (y — в локальных координатах экрана)
        val MenuX = 10f
        val HOME_Y = floatArrayOf(26f, 48f, 70f, 92f, 114f, 136f, 158f, 180f)
    }
}

/** Простая прямоугольная спецификация (rect из SVG). */
data class RectGeom(val x: Float, val y: Float, val w: Float, val h: Float, val rx: Float) {
    val topLeft: Offset get() = Offset(x, y)
    val size: Size get() = Size(w, h)
    val radius: CornerRadius get() = CornerRadius(rx)
}
