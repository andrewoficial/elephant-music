package ru.kantser.elephantmusic.ui.screens.player

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import ru.kantser.elephantmusic.ui.screens.player.SvgGeometry as Geo

/**
 * Дебаг-«чертёж» корпуса прибора: обводки ключевых прямоугольников геометрии
 * (BodyShell/BodyEdge/BodyFace/DisplayFrame) плюс маркеры углов. Рисуется в координатах
 * устройства (SvgGeometry 520x350); хост сам применяет масштаб — либо оверлей на реальном
 * приборе в PlayerDeviceView, либо letterbox в тестовом поле (RitmixBodyLayer).
 */
fun DrawScope.drawBodyBlueprint(showCorners: Boolean = true) {
    BodyBlueprintTargets.forEach { t ->
        drawRoundRect(
            color = t.color.copy(alpha = 0.9f),
            topLeft = t.g.topLeft,
            size = t.g.size,
            cornerRadius = t.g.radius,
            style = Stroke(1.5f),
        )
    }
    if (showCorners) {
        BodyBlueprintTargets.forEach { t ->
            cornersOf(t.g).forEach { c ->
                drawCircle(Color(0xFF00E5FF), radius = 3f, center = c, style = Stroke(1f))
                drawCircle(t.color, radius = 2f, center = c)
            }
        }
    }
}

private class BPColor(val color: Color, val g: RectGeom)

private val BodyBlueprintTargets = listOf(
    BPColor(Color(0xFF00E5FF), Geo.BodyShell),
    BPColor(Color(0xFFFF2D92), Geo.BodyEdge),
    BPColor(Color(0xFFFFEB3B), Geo.BodyFace),
    BPColor(Color(0xFF76FF03), Geo.DisplayFrame),
)

private fun cornersOf(g: RectGeom): List<Offset> = listOf(
    Offset(g.x, g.y),
    Offset(g.x + g.w, g.y),
    Offset(g.x + g.w, g.y + g.h),
    Offset(g.x, g.y + g.h),
)
