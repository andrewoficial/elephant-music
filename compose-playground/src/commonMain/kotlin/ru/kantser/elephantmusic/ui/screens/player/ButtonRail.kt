package ru.kantser.elephantmusic.ui.screens.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.kantser.elephantmusic.ui.screens.player.SvgGeometry as Geo
import ru.kantser.elephantmusic.ui.screens.player.SvgGradients as G

enum class DeviceButton { REWIND, MENU, PLAY_PAUSE, FWD }

/**
 * Кнопочная панель: статичный металлический брусок (общий цилиндр + глянец + фаски)
 * + 4 интерактивные зоны (hover/press) + шелкография иконок слева.
 * Отражает <g id="btnBlock"> SVG. Геометрия — в абсолютных координатах устройства.
 */
@Composable
fun ButtonRail(
    modifier: Modifier,
    onRewind: () -> Unit,
    onMenu: () -> Unit,
    onPlayPause: () -> Unit,
    onFwd: () -> Unit,
) {
    Box(modifier) {
        Canvas(Modifier.size(Geo.W.dp, Geo.H.dp)) {
            drawInDeviceSpace {
                drawRailMetal()
                drawGraphicIcons()
            }
        }
        drawMenuIcon()
        drawHitZones(onRewind, onMenu, onPlayPause, onFwd)
    }
}

private fun DrawScope.drawRailMetal() {
    val b = Geo.Buttons
    // Общий цилиндр металла по силуэту (btnMetal)
    for (i in 0 until 4) {
        val y = b.bodyY(i)
        drawRect(G.BarMetal, Offset(b.BodyX, y), Size(b.BodyW, b.BodyH))
    }
    // Лица кнопок: базовый серый + гориз. и верт. фаска
    for (i in 0 until 4) {
        val y = b.BodyY0 + i * b.Step
        val face = Rect(b.FaceX, y + 1f, b.FaceX + b.FaceW, y + 1f + b.FaceH)
        drawRect(G.ButtonFaceBase, face.topLeft, face.size)
        drawRect(G.FaceSide, face.topLeft, face.size)
        drawRect(G.FaceVert, face.topLeft, face.size)
    }
    // Единый диагональный зайчик, обрезанный по силуэту бруска
    val clip = Path().apply {
        for (i in 0 until 4) {
            val y = b.bodyY(i)
            addRect(Rect(b.BodyX, y, b.BodyX + b.BodyW, y + b.BodyH))
        }
    }
    clipPath(clip) {
        drawRect(
            brush = G.BarGlare(b.BodyW, 4 * b.Step),
            topLeft = Offset(b.BodyX, b.BodyY0),
            size = Size(b.BodyW, 4 * b.Step),
        )
    }
    // Тени пропилов (btnRim)
    for (i in 0 until 4) {
        val y = b.bodyY(i)
        drawRect(G.ButtonRim, Offset(b.BodyX, y), Size(b.BodyW, b.BodyH), style = Stroke(1f))
    }
}

private fun tri(d: String): Path = PathParser().parsePathString(d).toPath()

/** Графические иконки (стрелки/бары). "M" рисуется отдельно настоящим Text. */
internal fun DrawScope.drawGraphicIcons() {
    val c = G.IconColor
    // ReWind
    drawRect(c, Offset(410f, 102f), Size(2.2f, 12f))
    drawPath(tri("m 413.4,108 7.2,-6 v 12 z"), c)
    drawPath(tri("m 420.6,108 7.2,-6 v 12 z"), c)
    // Play / Pause
    drawPath(tri("m 410,182 8.5,6 -8.5,6 z"), c)
    drawRect(c, Offset(420.5f, 182f), Size(2.6f, 12f))
    drawRect(c, Offset(424.9f, 182f), Size(2.6f, 12f))
    // Fwd
    drawPath(tri("m 417,228 -7,-6 v 12 z"), c)
    drawPath(tri("m 425,228 -7,-6 v 12 z"), c)
    drawRect(c, Offset(425.8f, 222f), Size(2.2f, 12f))
}

/** Лейбл "M" (кнопка меню) на отладочном корпусе, рисуется текстом в канвасе. */
internal fun DrawScope.drawMenuLabel(measurer: TextMeasurer) {
    val tl = Geo.Buttons.Icons.MenuTopLeft
    val layout = measurer.measure(
        text = "M",
        style = TextStyle(
            color = G.IconColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        ),
    )
    drawText(textLayoutResult = layout, topLeft = tl)
}

@Composable
private fun drawMenuIcon() {
    val i = Geo.Buttons.Icons
    Text(
        "M",
        color = G.IconColor,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .offset(x = (i.MenuX - 6).dp, y = (i.MenuY - 14).dp),
    )
}

@Composable
private fun drawHitZones(
    onRewind: () -> Unit,
    onMenu: () -> Unit,
    onPlayPause: () -> Unit,
    onFwd: () -> Unit,
) {
    val b = Geo.Buttons
    val actions = mapOf(
        DeviceButton.REWIND to onRewind,
        DeviceButton.MENU to onMenu,
        DeviceButton.PLAY_PAUSE to onPlayPause,
        DeviceButton.FWD to onFwd,
    )
    DeviceButton.entries.forEach { btn ->
        val i = btn.ordinal
        val y = Geo.Buttons.HitZones.y + i * b.Step
        val hit = Rect(Geo.Buttons.HitZones.x, y, Geo.Buttons.HitZones.x + Geo.Buttons.HitZones.w, y + Geo.Buttons.HitZones.h)
        BlockHitZone(hit = hit, onClick = { actions[btn]?.invoke() })
    }
}

@Composable
private fun BlockHitZone(hit: Rect, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val pressed by interaction.collectIsPressedAsState()

    val b = Geo.Buttons
    val faceX = hit.left + (Geo.Buttons.FaceX - Geo.Buttons.HitZones.x)
    val faceY = hit.top + (hit.height - Geo.Buttons.FaceH) / 2f

    Box(
        Modifier
            .offset(x = hit.left.dp, y = hit.top.dp)
            .size(hit.width.dp, hit.height.dp)
            .hoverable(interaction)
            .clickable(interactionSource = interaction, indication = null) { onClick() },
        contentAlignment = Alignment.TopStart,
    ) {
        val overlayColor = when {
            pressed -> Color.Black.copy(alpha = 0.30f)
            hovered -> Color.White.copy(alpha = 0.22f)
            else -> Color.Transparent
        }
        if (overlayColor != Color.Transparent) {
            Box(
                Modifier
                    .offset(x = (faceX - hit.left).dp, y = (faceY - hit.top).dp)
                    .size(b.FaceW.dp, b.FaceH.dp)
                    .background(overlayColor),
            )
        }
    }
}
