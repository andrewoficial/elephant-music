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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ru.kantser.elephantmusic.ui.screens.test.gui.GuiGeometry as Geo

/** Имена углов в порядке RectGeom.corners: [левый-верх, правый-верх, правый-низ, левый-низ]. */
private val CornerNames = listOf("ЛВ", "ПВ", "ПН", "ЛН")

/**
 * Отладочная панель: координаты углов фигур (копируемо), легенда координат, показания
 * окна/области поля и ползунки настройки масштаба (мин/макс/ручной множитель).
 */
@Composable
fun DebugPanel(
    showCorners: Boolean,
    onToggleCorners: () -> Unit,
) {
    val resolution: GuiResolutionService = koinInject()
    val dpi: GuiDpiService = koinInject()
    val scale: GuiScaleService = koinInject()
    val debug: GuiDebugState = koinInject()
    val density = LocalDensity.current

    fun fmt(g: RectGeom): String =
        "[" + g.corners.mapIndexed { i, c -> "${CornerNames[i]}(${c.x.toInt()},${c.y.toInt()})" }
            .joinToString(" ") + "]"

    val winLine = debug.windowSize?.let { "окно ${it.width.value.toInt()}x${it.height.value.toInt()} dp" } ?: "окно: нет данных"
    val fieldLine = "область поля ${debug.fieldPx.width.toInt()}x${debug.fieldPx.height.toInt()} dp"
    val redPxW = (Geo.W * debug.appliedScale * density.density).toInt()
    val redPxH = (Geo.H * debug.appliedScale * density.density).toInt()
    val redLine = "Красный (300x300 логич.) = $redPxW x $redPxH px при масштабе ${debug.appliedScale}"

    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text("Координаты: (0,0) — левый верхний угол поля; x растёт вправо, y — вниз. " +
            "Углы: ЛВ/ПВ/ПН/ЛН.", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(
            "Показывать углы: ${if (showCorners) "вкл" else "выкл"} (клик по полю)",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 2.dp).clickable { onToggleCorners() },
        )
        Spacer(Modifier.height(4.dp))

        SelectionContainer {
            Column {
                Text("Отладка SVG", style = MaterialTheme.typography.titleMedium)
                Text(winLine, style = MaterialTheme.typography.bodySmall)
                Text(fieldLine, style = MaterialTheme.typography.bodySmall)
                Text(redLine, style = MaterialTheme.typography.bodySmall)
                Text(resolution.activeDisplay()?.let {
                    "экран ${it.widthPx}x${it.heightPx}px dpi=${it.densityDpi.toInt()} ос-масштаб=${it.scaleFactor}"
                } ?: "экран: нет данных", style = MaterialTheme.typography.bodySmall)
                Text("Compose density = ${dpi.densityFactor(density)}", style = MaterialTheme.typography.bodySmall)
                Text("Красный ${fmt(Geo.Red)}", style = MaterialTheme.typography.bodySmall)
                Text("Синий  ${fmt(Geo.Blue)}", style = MaterialTheme.typography.bodySmall)
                Text("Белый  ${fmt(Geo.White)}", style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(6.dp))

        ScaleSliders(scale)
    }
}

@Composable
private fun ScaleSliders(scale: GuiScaleService) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("мин", style = MaterialTheme.typography.labelSmall)
        Slider(
            value = scale.minScale,
            onValueChange = { scale.minScale = it },
            valueRange = 0.05f..1f,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
        )
        Text("${scale.minScale}", style = MaterialTheme.typography.labelSmall)
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("макс", style = MaterialTheme.typography.labelSmall)
        Slider(
            value = scale.maxScale,
            onValueChange = { scale.maxScale = it },
            valueRange = 1f..10f,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
        )
        Text("${scale.maxScale}", style = MaterialTheme.typography.labelSmall)
    }
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
