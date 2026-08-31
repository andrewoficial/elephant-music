package ru.kantser.elephantmusic.ui.screens.test.gui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import ru.kantser.elephantmusic.ui.screens.player.HOME_ITEMS
import ru.kantser.elephantmusic.ui.screens.player.SvgGradients as G
import ru.kantser.elephantmusic.ui.screens.player.SvgGeometry as PlayerGeo

/**
 * Слой главного меню экрана: окно пунктов (HOME_ITEMS) в светлой зоне + подсветка выбранного
 * (G.SelectedFill во всю ширину светлой зоны и высоту строки, как в оригинале)
 * + пиктограмма выбранного пункта на синей боковой панели.
 *
 * Самостоятельный DeviceLayer (SRP): вся логика меню живёт здесь, а не в оркестраторе
 * RitmixBodyLayer (OCP — добавить/убрать меню = правка списка слоёв). Геометрия —
 * в локальных координатах рамки дисплея.
 */
internal class MenuListLayer(
    private val measurer: TextMeasurer,
    private val menuIndex: Int,
    private val scrollIndex: Int,
) : DeviceLayer {
    override fun draw(scope: DrawScope) = with(scope) {
        drawTopBar()
        drawMenuList(measurer, menuIndex, scrollIndex)
        drawMenuPanelIcon(menuIndex)
    }
}

/** Метрики раскладки списка меню (количество строк = MenuScroll.VISIBLE_ROWS). */
private class MenuMetrics(
    val startY: Float,      // y первого видимого пункта (frame-local)
    val pitch: Float,       // шаг по вертикали (frame-local)
    val rowHeight: Float,   // высота отрисовки пункта (pitch - 1: пункты на 1px уже)
)

private fun menuMetrics(sc: PlayerGeo.Screen): MenuMetrics {
    val n = MenuScroll.VISIBLE_ROWS
    val contentTop = sc.LightY + sc.TopBarH
    val contentBottom = sc.LightY + sc.LightH
    val pitch = (contentBottom - contentTop) / n
    return MenuMetrics(startY = contentTop, pitch = pitch, rowHeight = pitch - 1f)
}

/** Пиктограмма выбранного пункта меню на синей боковой панели, по центру. */
private fun DrawScope.drawMenuPanelIcon(menuIndex: Int) {
    val origin = PlayerGeo.DisplayFrame.topLeft
    val p = PlayerGeo.Screen
    val iconSize = 80f
    val cx = origin.x + p.PanelX + p.PanelW / 2f
    val cy = origin.y + p.PanelY + p.PanelH / 2f
    withTransform({
        translate(cx - iconSize / 2f, cy - iconSize / 2f)
        scale(iconSize / 200f, iconSize / 200f, pivot = Offset.Zero)
    }) {
        MenuIcons.drawMenuIcon(this, menuIndex)
    }
}

/**
 * Верхняя «шапка» окна: затемнённый фон с белыми (светло-серыми) однопиксельными
 * полосками вдоль верхней и нижней грани, высотой TopBarH, на всю ширину светлой зоны.
 * В главном меню текст шапки пустой.
 */
private fun DrawScope.drawTopBar() {
    val origin = PlayerGeo.DisplayFrame.topLeft
    val sc = PlayerGeo.Screen

    val tl = Offset(origin.x + sc.LightX, origin.y + sc.LightY)
    val size = Size(sc.LightW, sc.TopBarH)

    // Затемнённая подложка поверх светлого фона.
    drawRect(Color(0xFF0b141c).copy(alpha = 0.40f), tl, size)

    // Полоски (тоньше 1px) только по верхней и нижней грани.
    val stripe = Color(0xFFe6edf2).copy(alpha = 0.60f)
    drawRect(stripe, Offset(tl.x, tl.y), Size(sc.LightW, 0.5f))                     // верхняя грань
    drawRect(stripe, Offset(tl.x, tl.y + sc.TopBarH - 0.5f), Size(sc.LightW, 0.5f)) // нижняя грань

    drawBatteryInHeader()
}

/** Батарейка у правой границы шапки, в 3 раза меньше по высоте, белая. */
private fun DrawScope.drawBatteryInHeader() {
    val origin = PlayerGeo.DisplayFrame.topLeft
    val sc = PlayerGeo.Screen

    val cy = origin.y + sc.LightY + sc.TopBarH / 2f
    val headerRight = origin.x + sc.LightX + sc.LightW

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

/**
 * Главное меню (HOME_ITEMS). Отрисовывается только окно [scrollIndex, scrollIndex+N),
 * привязанное к верху области контента экрана; пункты, выходящие за окно, не рисуются
 * (это и есть «листалка»). Выбранный пункт всегда в окне — подсветка никогда не исчезает.
 * Смещение окна управляется из TestScreen через [MenuScroll.fitScroll].
 */
private fun DrawScope.drawMenuList(measurer: TextMeasurer, menuIndex: Int, scrollIndex: Int) {
    val origin = PlayerGeo.DisplayFrame.topLeft
    val sc = PlayerGeo.Screen
    val m = menuMetrics(sc)

    val left = origin.x + sc.LightX
    val top = origin.y + m.startY
    val right = left + sc.LightW
    val bottom = origin.y + m.startY + MenuScroll.VISIBLE_ROWS * m.pitch

    clipRect(left = left, top = top, right = right, bottom = bottom) {
        val style = TextStyle(color = Color.Red, fontSize = 13.sp)
        val visible = (scrollIndex until scrollIndex + MenuScroll.VISIBLE_ROWS)
            .filter { it in HOME_ITEMS.indices }

        // Подсветка выбранного пункта (всегда в окне).
        val hlY = origin.y + m.startY + (menuIndex - scrollIndex) * m.pitch
        drawRoundRect(
            G.SelectedFill,
            Offset(origin.x + sc.LightX, hlY),
            Size(sc.LightW, m.rowHeight),
            CornerRadius(2f),
        )

        // Пункты окна.
        visible.forEach { i ->
            val layout = measurer.measure(HOME_ITEMS[i], style)
            val y = origin.y + m.startY + (i - scrollIndex) * m.pitch
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(origin.x + sc.MenuX, y),
            )
        }
    }
}
