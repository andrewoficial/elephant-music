package ru.kantser.elephantmusic.ui.screens.test.gui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import ru.kantser.elephantmusic.ui.screens.player.SvgGeometry as PlayerGeo

/**
 * Верхняя «шапка» окна (всегда поверх контента): затемнённый фон с белыми (светло-серыми)
 * полосками вдоль верхней и нижней грани + белая батарейка у правого края.
 *
 * Ширина зависит от режима экрана: в главном меню — светлая зона ([fullWidth]=false), в открытом
 * разделе ([fullWidth]=true) — на всю ширину экрана.
 */
internal class TopBarLayer(private val fullWidth: Boolean) : DeviceLayer {
    override fun draw(scope: DrawScope) = with(scope) {
        val origin = PlayerGeo.DisplayFrame.topLeft
        val sc = PlayerGeo.Screen
        val width = if (fullWidth) FullContentWidth else sc.LightW
        val tl = Offset(origin.x + sc.LightX, origin.y + sc.LightY)
        val size = Size(width, sc.TopBarH)

        // Затемнённая подложка поверх светлого фона.
        drawRect(Color(0xFF0b141c).copy(alpha = 0.40f), tl, size)

        // Полоски (тоньше 1px) только по верхней и нижней грани.
        val stripe = Color(0xFFe6edf2).copy(alpha = 0.60f)
        drawRect(stripe, Offset(tl.x, tl.y), Size(width, 0.5f))                     // верхняя грань
        drawRect(stripe, Offset(tl.x, tl.y + sc.TopBarH - 0.5f), Size(width, 0.5f)) // нижняя грань

        drawBatteryInHeader(width)
    }

    /** Батарейка у правой границы шапки, в 3 раза меньше по высоте, белая. */
    private fun DrawScope.drawBatteryInHeader(width: Float) {
        val origin = PlayerGeo.DisplayFrame.topLeft
        val sc = PlayerGeo.Screen

        val cy = origin.y + sc.LightY + sc.TopBarH / 2f
        val headerRight = origin.x + sc.LightX + width

        // Контент батарейки в раскладке 200×200.
        val contentMinX = 35f
        val contentMaxX = 184f
        val contentMinY = 64f
        val contentMaxY = 136f
        val contentW = contentMaxX - contentMinX
        val contentH = contentMaxY - contentMinY

        // В 3 раза меньше исходного масштаба, у правой границы шапки.
        val baseScale = sc.TopBarH / contentH
        val scale = baseScale / 3f

        // Отступ от правого края примерно на ширину двух букв (≈16px в масштабе шапки).
        val rightMargin = 16f
        val leftX = headerRight - rightMargin - contentW * scale
        val topY = cy - contentH / 2f * scale

        withTransform({
            translate(leftX - contentMinX * scale, topY - contentMinY * scale)
            scale(scale, scale, pivot = Offset.Zero)
        }) {
            MenuIcons.drawBatteryIcon(this, color = Color.White)
        }
    }
}
