package ru.kantser.elephantmusic.ui.screens.test.gui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import ru.kantser.elephantmusic.ui.screens.player.RitmixLogo
import ru.kantser.elephantmusic.ui.screens.player.SvgGradients as G
import ru.kantser.elephantmusic.ui.screens.player.drawBodyBlueprint
import ru.kantser.elephantmusic.ui.screens.player.drawPlayerBody
import ru.kantser.elephantmusic.ui.screens.player.drawPlayerFrame
import ru.kantser.elephantmusic.ui.screens.player.SvgGeometry as PlayerGeo

/**
 * Дебаг-слой «реального корпуса плеера» внутри тестового поля: воспроизводит отрисовку
 * Ritmix (корпус → рамка дисплея → экран) в координатах устройства 520x350, вписанных в
 * поле GuiGeometry (300x300) по правилу letterbox (по меньшей стороне, по центру).
 * Сверху — тонкий чертёж прямоугольников (drawBodyBlueprint) как оверлей при showOutlines.
 */
object RitmixBodyLayer {
    @Composable
    fun View(showOutlines: Boolean, modifier: Modifier = Modifier) {
        Canvas(modifier) { drawInDeviceSpace { draw(showOutlines) } }
    }

    fun DrawScope.draw(showOutlines: Boolean) {
        val s = minOf(GuiGeometry.W / PlayerGeo.W, GuiGeometry.H / PlayerGeo.H)
        val dx = (GuiGeometry.W - PlayerGeo.W * s) / 2f
        val dy = (GuiGeometry.H - PlayerGeo.H * s) / 2f
        withTransform({
            translate(dx, dy)
            scale(s, s, pivot = Offset.Zero)
        }) {
            // Слои прибора в реальном порядке: корпус → логотип → рамка → экран → (чертёж).
            drawPlayerBody()
            RitmixLogo.draw(this)
            drawPlayerFrame()
            drawScreenBackground()
            if (showOutlines) drawBodyBlueprint(showCorners = true)
        }
    }

    /** Экран (светлый фон + синяя панель) в локальных координатах рамки дисплея. */
    private fun DrawScope.drawScreenBackground() {
        val origin = PlayerGeo.DisplayFrame.topLeft
        val sc = PlayerGeo.Screen
        drawRoundRect(
            G.ScreenBg,
            Offset(origin.x + sc.LightX, origin.y + sc.LightY),
            Size(sc.LightW, sc.LightH),
            CornerRadius(2f),
        )
        drawRoundRect(
            G.PanelBlue,
            Offset(origin.x + sc.PanelX, origin.y + sc.PanelY),
            Size(sc.PanelW, sc.PanelH),
            CornerRadius(2f),
        )
    }
}
