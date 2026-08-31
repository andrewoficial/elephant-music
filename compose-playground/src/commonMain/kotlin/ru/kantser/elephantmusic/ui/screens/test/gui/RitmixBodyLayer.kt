package ru.kantser.elephantmusic.ui.screens.test.gui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import ru.kantser.elephantmusic.platform.AppLog
import ru.kantser.elephantmusic.ui.screens.player.DeviceButton
import ru.kantser.elephantmusic.ui.screens.player.HOME_ITEMS
import ru.kantser.elephantmusic.ui.screens.player.RitmixLogo
import ru.kantser.elephantmusic.ui.screens.player.SvgGradients as G
import ru.kantser.elephantmusic.ui.screens.player.drawBodyBlueprint
import ru.kantser.elephantmusic.ui.screens.player.drawGraphicIcons
import ru.kantser.elephantmusic.ui.screens.player.drawMenuLabel
import ru.kantser.elephantmusic.ui.screens.player.drawPlayerBody
import ru.kantser.elephantmusic.ui.screens.player.drawRailMetal
import ru.kantser.elephantmusic.ui.screens.player.drawPlayerFrame
import ru.kantser.elephantmusic.ui.screens.player.SvgGeometry as PlayerGeo

/**
 * Дебаг-слой «реального корпуса плеера» внутри тестового поля: воспроизводит отрисовку
 * Ritmix (корпус → рамка дисплея → экран → значки кнопок) в координатах устройства 520x350,
 * вписанных в поле GuiGeometry (300x300) по правилу letterbox (по меньшей стороне, по центру).
 * Сверху — тонкий чертёж прямоугольников (drawBodyBlueprint) как оверлей при showOutlines.
 */
object RitmixBodyLayer {
    /** Печать в терминал координат кнопочного бруска и значков/лейбла (один раз при входе в BODY). */
    fun logIconCoords(log: AppLog) {
        val tag = "DebugSVG"
        val b = PlayerGeo.Buttons
        log.i(tag, "Rail   bodyBounds=${fmtBox(RectGeomP(b.BodyX, b.BodyY0, b.BodyW, 4 * b.Step))} step=${b.Step}")
        ru.kantser.elephantmusic.ui.screens.player.DeviceButton.entries.forEachIndexed { n, btn ->
            val y = b.bodyY(n)
            log.i(tag, "Rail   ${btn.name.padEnd(11)}body=${fmtBox(RectGeomP(b.BodyX, y, b.BodyW, b.BodyH))}")
        }
        log.i(tag, "Icons  Rewind  bounds=${fmtBox(PlayerGeo.Buttons.Icons.RewindBox)}")
        log.i(tag, "Icons  Menu(M) topLeft=(${PlayerGeo.Buttons.Icons.MenuTopLeft.x.toInt()},${PlayerGeo.Buttons.Icons.MenuTopLeft.y.toInt()})")
        log.i(tag, "Icons  Play/Pause  bounds=${fmtBox(PlayerGeo.Buttons.Icons.PlayBox)}")
        log.i(tag, "Icons  Fwd  bounds=${fmtBox(PlayerGeo.Buttons.Icons.FwdBox)}")
    }

    private fun RectGeomP(x: Float, y: Float, w: Float, h: Float) =
        ru.kantser.elephantmusic.ui.screens.player.RectGeom(x, y, w, h, 0f)

    private fun fmtBox(g: ru.kantser.elephantmusic.ui.screens.player.RectGeom): String =
        "(${g.x.toInt()},${g.y.toInt()})..(${(g.x + g.w).toInt()},${(g.y + g.h).toInt()})"

    /** Параметры letterbox-вписывания устройства 520x350 в поле GuiGeometry (300x300). */
    private class Letterbox(val s: Float, val dx: Float, val dy: Float)

    private fun letterbox(): Letterbox {
        val s = minOf(GuiGeometry.W / PlayerGeo.W, GuiGeometry.H / PlayerGeo.H)
        return Letterbox(
            s,
            (GuiGeometry.W - PlayerGeo.W * s) / 2f,
            (GuiGeometry.H - PlayerGeo.H * s) / 2f,
        )
    }

    /** Невидимые кликабельные зоны кнопок поверх Canvas, в тех же letterbox-координатах. */
    @Composable
    private fun ButtonHitZones(lb: Letterbox, onButton: (DeviceButton) -> Unit) {
        val hz = PlayerGeo.Buttons.HitZones
        val step = PlayerGeo.Buttons.Step
        DeviceButton.entries.forEachIndexed { i, btn ->
            val x = lb.dx + hz.x * lb.s
            val y = lb.dy + (hz.y + i * step) * lb.s
            Box(
                Modifier
                    .offset(x = x.dp, y = y.dp)
                    .size((hz.w * lb.s).dp, (hz.h * lb.s).dp)
                    .clickable { onButton(btn) },
            )
        }
    }

    @Composable
    fun View(
        showOutlines: Boolean,
        onButton: (DeviceButton) -> Unit,
        menuIndex: Int = 0,
        modifier: Modifier = Modifier,
    ) {
        val log: AppLog = koinInject()
        val measurer = rememberTextMeasurer()
        LaunchedEffect(Unit) { logIconCoords(log) }

        val lb = letterbox()
        Box(modifier) {
            Canvas(Modifier.fillMaxSize()) {
                drawInDeviceSpace {
                    draw(measurer, showOutlines, menuIndex)
                }
            }
            ButtonHitZones(lb, onButton)
        }
    }

    fun DrawScope.draw(measurer: TextMeasurer, showOutlines: Boolean, menuIndex: Int = 0) {
        val lb = letterbox()
        withTransform({
            translate(lb.dx, lb.dy)
            scale(lb.s, lb.s, pivot = Offset.Zero)
        }) {
            // Слои прибора в реальном порядке: корпус → логотип → рамка → экран → кнопки → (чертёж).
            // Благодаря DeviceLayer оркестратор не знает устройства слоёв (OCP): кнопки — это слой,
            // добавить/убрать их можно просто правкой списка, не трогая оркестратор.
            val layers: List<DeviceLayer?> = listOf(
                BodyLayer,
                LogoLayer,
                FrameLayer,
                ScreenLayer,
                MenuListLayer(measurer, menuIndex),
                ButtonsLayer(measurer),
                if (showOutlines) OutlinesLayer else null,
            )
            layers.forEach { layer -> layer?.draw(this) }
        }
    }

    private object BodyLayer : DeviceLayer {
        override fun draw(scope: DrawScope) = with(scope) { drawPlayerBody() }
    }

    private object LogoLayer : DeviceLayer {
        override fun draw(scope: DrawScope) = with(scope) { RitmixLogo.draw(this) }
    }

    private object FrameLayer : DeviceLayer {
        override fun draw(scope: DrawScope) = with(scope) { drawPlayerFrame() }
    }

    private object ScreenLayer : DeviceLayer {
        override fun draw(scope: DrawScope) = with(scope) { drawScreenBackground() }
    }

    /**
     * Один пункт + подсветка выбранного (прямоугольник G.SelectedFill во всю ширину светлой
     * зоны и высоту строки, как в оригинале). Параметры — в локальных координатах рамки дисплея.
     */
    private class MenuListLayer(
        private val measurer: TextMeasurer,
        private val menuIndex: Int,
    ) : DeviceLayer {
        override fun draw(scope: DrawScope) = with(scope) { drawMenuList(measurer, menuIndex) }
    }

    /** Физический кнопочный брусок: металл + фаски (drawRailMetal) + шелкография (значки и лейбл "M"). */
    private class ButtonsLayer(private val measurer: TextMeasurer) : DeviceLayer {
        override fun draw(scope: DrawScope) = with(scope) {
            drawRailMetal()
            drawButtonEdgeHighlight()
            drawGraphicIcons()
            drawMenuLabel(measurer)
        }
    }

    private object OutlinesLayer : DeviceLayer {
        override fun draw(scope: DrawScope) = with(scope) { drawBodyBlueprint(showCorners = true) }
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

    /**
     * Пункты главного меню (HOME_ITEMS) в светлой зоне экрана.
     * Координаты берутся из Screen в локальных координатах рамки дисплея (MenuX, HOME_Y),
     * поэтому совпадают с отрисовкой экрана в drawScreenBackground.
     *
     * Отрисовка ограничена областью контента экрана (ниже верхней полосы TopBarH): пункты,
     * выходящие за её нижнюю границу, не показываются. Это решает «вылезание» текста за экран
     * и является основой для будущей прокрутки списка (сдвиг строк внутри этого клипа).
     * Выделение пункта намеренно не рисуется — оно появится отдельным шагом.
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

    /**
     * Локальная компенсация (вариант B): при letterbox-масштабе тонкая светлая фаска кнопок
     * (FaceSide/FaceVert) сжимается до ~1px и «усредняется», отчего грань выглядит блеклой.
     * Здесь рисуем поверх лица каждой кнопки явную яркую кромку (слева + сверху), которая
     * остаётся видимой и после масштаба. Затрагивает только тестовую вкладку.
     */
    private fun DrawScope.drawButtonEdgeHighlight() {
        val b = PlayerGeo.Buttons
        val highlight = Color(0xFFd8dde1)
        for (i in 0 until 4) {
            val y = b.bodyY(i) + 1f
            val left = b.FaceX
            val top = y
            val right = b.FaceX + b.FaceW
            val bottom = y + b.FaceH
            // Светлая левая грань
            drawLine(highlight, Offset(left, top), Offset(left, bottom), strokeWidth = 1.6f)
            // Светлая верхняя грань
            drawLine(highlight, Offset(left, top), Offset(right, top), strokeWidth = 1.6f)
            // Тонкая правая/нижняя тень для контраста грани
            drawLine(
                Color.Black.copy(alpha = 0.35f),
                Offset(right, top),
                Offset(right, bottom),
                strokeWidth = 1f,
            )
            drawLine(
                Color.Black.copy(alpha = 0.25f),
                Offset(left, bottom),
                Offset(right, bottom),
                strokeWidth = 1f,
            )
        }
    }
}
