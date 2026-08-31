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
 * Слой главного меню экрана: пункты (HOME_ITEMS) в светлой зоне + подсветка выбранного
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
) : DeviceLayer {
    override fun draw(scope: DrawScope) = with(scope) {
        drawMenuList(measurer, menuIndex)
        drawMenuPanelIcon(menuIndex)
    }
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
 * Пункты главного меню (HOME_ITEMS) в светлой зоне экрана.
 * Координаты берутся из Screen в локальных координатах рамки дисплея (MenuX, HOME_Y),
 * поэтому совпадают с отрисовкой экрана в drawScreenBackground.
 *
 * Отрисовка ограничена областью контента экрана (ниже верхней полосы TopBarH): пункты,
 * выходящие за её нижнюю границу, не показываются. Это решает «вылезание» текста за экран
 * и является основой для будущей прокрутки списка (сдвиг строк внутри этого клипа).
 */
private fun DrawScope.drawMenuList(measurer: TextMeasurer, menuIndex: Int) {
    val origin = PlayerGeo.DisplayFrame.topLeft
    val sc = PlayerGeo.Screen
    val left = origin.x + sc.LightX
    val top = origin.y + sc.LightY + sc.TopBarH
    val right = left + sc.LightW
    val bottom = top + (sc.LightH - sc.TopBarH)

    clipRect(left = left, top = top, right = right, bottom = bottom) {
        val rowHeight = sc.HOME_Y[1] - sc.HOME_Y[0]
        val sel = menuIndex.coerceIn(HOME_ITEMS.indices)

        // Подсветка выбранного пункта: прямоугольник во всю ширину светлой зоны и высоту строки.
        val hlTopLeft = Offset(origin.x + sc.LightX, origin.y + sc.HOME_Y[sel])
        drawRoundRect(
            G.SelectedFill,
            hlTopLeft,
            Size(sc.LightW, rowHeight),
            CornerRadius(2f),
        )

        // Пункты меню поверх подсветки.
        val style = TextStyle(color = Color.Red, fontSize = 13.sp)
        HOME_ITEMS.forEachIndexed { i, label ->
            val layout = measurer.measure(label, style)
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(origin.x + sc.MenuX, origin.y + sc.HOME_Y[i]),
            )
        }
    }
}
