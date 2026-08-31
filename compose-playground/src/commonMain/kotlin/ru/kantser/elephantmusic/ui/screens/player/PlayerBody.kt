package ru.kantser.elephantmusic.ui.screens.player

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import ru.kantser.elephantmusic.ui.screens.player.SvgGradients as G
import ru.kantser.elephantmusic.ui.screens.player.SvgGeometry as Geo

/**
 * Корпус прибора: отражает <rect> элементы SVG
 * (bodyShell, bodyEdge, bodyFace, bodyGloss).
 * Рисуется в логических координатах устройства (520x350).
 */
object PlayerBody {
    @Composable
    fun View(modifier: Modifier = androidx.compose.ui.Modifier) {
        Canvas(modifier) { drawInDeviceSpace { drawPlayerBody() } }
    }
}

/** Отрисовка реального корпуса (шелл/фаска/фейс/блик) в координатах устройства 520x350. */
fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPlayerBody() {
    val shell = Geo.BodyShell
    val face = Geo.BodyFace
    // bodyShell — закруглённая пластина с глянцем + тонкий контур
    drawRoundRect(G.BodyFill, shell.topLeft, shell.size, shell.radius)
    drawRoundRect(color = G.BodyStroke, topLeft = shell.topLeft, size = shell.size, cornerRadius = shell.radius, style = Stroke(1.5f))
    // bodyEdge — чуть больше фейс, контур фаски
    val edge = Geo.BodyEdge
    drawRoundRect(color = G.BodyEdgeStroke, topLeft = edge.topLeft, size = edge.size, cornerRadius = edge.radius, style = Stroke(1f))
    // bodyFace — тёмная внутренняя панель
    drawRoundRect(G.BodyFaceFill, face.topLeft, face.size, face.radius)
    // bodyGloss — блик по лицу
    drawRoundRect(G.FaceGloss, face.topLeft, face.size, face.radius)
}
