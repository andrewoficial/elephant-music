package ru.kantser.elephantmusic.ui.screens.test.gui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ru.kantser.elephantmusic.platform.AppLog
import ru.kantser.elephantmusic.ui.screens.player.SvgGeometry as PlayerGeo
import ru.kantser.elephantmusic.ui.screens.test.gui.GuiGeometry as Geo

/** Имена углов в порядке corners: [левый-верх, правый-верх, правый-низ, левый-низ]. */
private val CornerNames = listOf("ЛВ", "ПВ", "ПН", "ЛН")

/**
 * Отладочная панель: координаты углов фигур (копируемо), легенда координат, показания
 * окна/области поля, переключатель вида (поле/корпус плеера), оверлей на плеере
 * и ползунки настройки масштаба (мин/макс/ручной множитель).
 */
@Composable
fun DebugPanel(
    mode: DebugViewMode,
    onModeChange: (DebugViewMode) -> Unit,
    showCorners: Boolean,
    onToggleCorners: () -> Unit,
) {
    val resolution: GuiResolutionService = koinInject()
    val dpi: GuiDpiService = koinInject()
    val scale: GuiScaleService = koinInject()
    val debug: GuiDebugState = koinInject()
    val log: AppLog = koinInject()
    val density = LocalDensity.current

    fun fmt(g: RectGeom): String =
        "[" + g.corners.mapIndexed { i, c -> "${CornerNames[i]}(${c.x.toInt()},${c.y.toInt()})" }
            .joinToString(" ") + "]"

    fun fmtP(g: ru.kantser.elephantmusic.ui.screens.player.RectGeom): String =
        "[" + listOf(
            "ЛВ(${g.x.toInt()},${g.y.toInt()})",
            "ПВ(${(g.x + g.w).toInt()},${g.y.toInt()})",
            "ПН(${(g.x + g.w).toInt()},${(g.y + g.h).toInt()})",
            "ЛН(${g.x.toInt()},${(g.y + g.h).toInt()})",
        ).joinToString(" ") + "]"

    LaunchedEffect(mode) {
        if (mode == DebugViewMode.BODY) {
            log.i("DebugSVG", "device=${PlayerGeo.W.toInt()}x${PlayerGeo.H.toInt()} guiScale=${debug.appliedScale}")
            log.i("DebugSVG", "Shell   ${fmtP(PlayerGeo.BodyShell)}")
            log.i("DebugSVG", "Edge    ${fmtP(PlayerGeo.BodyEdge)}")
            log.i("DebugSVG", "Frame   ${fmtP(PlayerGeo.DisplayFrame)}")
        }
    }

    val winLine = debug.windowSize?.let { "окно ${it.width.value.toInt()}x${it.height.value.toInt()} dp" } ?: "окно: нет данных"
    val fieldLine = "область поля ${debug.fieldPx.width.toInt()}x${debug.fieldPx.height.toInt()} dp"
    val densityLine = "compose density = ${dpi.densityFactor(density)}"
    val infoLine = "$winLine    $fieldLine    $densityLine"
    val redPxW = (Geo.W * debug.appliedScale * density.density).toInt()
    val redPxH = (Geo.H * debug.appliedScale * density.density).toInt()
    val redLine = "Красный (300x300 логич.) = $redPxW x $redPxH px при масштабе ${debug.appliedScale}"

    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("вид: ", style = MaterialTheme.typography.bodySmall)
            Text(
                "поле",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable { onModeChange(DebugViewMode.FIELD) },
                color = if (mode == DebugViewMode.FIELD) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
            Text(" / ", style = MaterialTheme.typography.bodySmall)
            Text(
                "корпус",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable { onModeChange(DebugViewMode.BODY) },
                color = if (mode == DebugViewMode.BODY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.weight(1f))
            Text(
                "углы: ${if (showCorners) "вкл" else "выкл"} (клик по полю)",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable { onToggleCorners() },
            )
        }
        Text(
            "Оверлей корпуса на плеере: ${if (debug.showPlayerBody) "вкл" else "выкл"}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 2.dp).clickable { debug.showPlayerBody = !debug.showPlayerBody },
        )
        Spacer(Modifier.height(4.dp))

        SelectionContainer {
            Column {
                Text("Отладка SVG", style = MaterialTheme.typography.titleMedium)
                Text(infoLine, style = MaterialTheme.typography.bodySmall)
                if (mode == DebugViewMode.FIELD) {
                    Text(redLine, style = MaterialTheme.typography.bodySmall)
                }
                Text(resolution.activeDisplay()?.let {
                    "экран ${it.widthPx}x${it.heightPx}px dpi=${it.densityDpi.toInt()} ос-масштаб=${it.scaleFactor}"
                } ?: "экран: нет данных", style = MaterialTheme.typography.bodySmall)
                if (mode == DebugViewMode.FIELD) {
                    Text("Красный ${fmt(Geo.Red)}", style = MaterialTheme.typography.bodySmall)
                    Text("Синий  ${fmt(Geo.Blue)}", style = MaterialTheme.typography.bodySmall)
                    Text("Белый  ${fmt(Geo.White)}", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("Shell  ${fmtP(PlayerGeo.BodyShell)}", style = MaterialTheme.typography.bodySmall)
                    Text("Edge   ${fmtP(PlayerGeo.BodyEdge)}", style = MaterialTheme.typography.bodySmall)
                    Text("Face   ${fmtP(PlayerGeo.BodyFace)}", style = MaterialTheme.typography.bodySmall)
                    Text("Frame  ${fmtP(PlayerGeo.DisplayFrame)}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        ScaleSliders(scale)
    }
}

@Composable
private fun ScaleSliders(scale: GuiScaleService) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("множ", style = MaterialTheme.typography.labelSmall)
        Slider(
            value = scale.manualFactor,
            onValueChange = { scale.manualFactor = it },
            valueRange = 0.1f..3f,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
        )
        Text("${scale.manualFactor}", style = MaterialTheme.typography.labelSmall)
    }
}
