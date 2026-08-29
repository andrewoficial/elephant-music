package ru.kantser.elephantmusic.ui.screens.test.gui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ru.kantser.elephantmusic.platform.AppLog
import ru.kantser.elephantmusic.ui.screens.test.gui.GuiGeometry as Geo

/**
 * Композиционный корень тестового поля (аналог PlayerDeviceView у Ritmix):
 * логический холст GuiGeometry.W x GuiGeometry.H, масштабируется под доступную площадь
 * через GuiScaleService. Слои снизу вверх: красный → синий → белый → маркеры углов.
 */
@Composable
fun TestDeviceView(
    showCorners: Boolean,
    onToggleCorners: () -> Unit,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        DeviceScale { scaledContent(showCorners, onToggleCorners) }
    }
}

@Composable
private fun scaledContent(showCorners: Boolean, onToggleCorners: () -> Unit) {
    Box(
        Modifier
            .size(Geo.W.dp, Geo.H.dp)
            .clickable { onToggleCorners() },
        contentAlignment = Alignment.TopStart,
    ) {
        RectLayerRed.View(Modifier.matchParentSize())
        RectLayerBlue.View(Modifier.matchParentSize())
        RectLayerWhite.View(Modifier.matchParentSize())
        DebugCornersLayer.View(showCorners, Modifier.matchParentSize())
    }
}

/**
 * Замеряет реально доступную область окна (в dp) и через GuiScaleService считает масштаб,
 * которым поле вписывается в эту площадь. Масштаб зависит от %available% и плотности,
 * поэтому пересчитывается при изменении любого из них. Логирует смену масштаба.
 */
@Composable
private fun DeviceScale(content: @Composable () -> Unit) {
    val log: AppLog = koinInject()
    val scaleService: GuiScaleService = koinInject()
    val debug: GuiDebugState = koinInject()
    val density = LocalDensity.current

    var availPx by remember { mutableStateOf(IntSize.Zero) }
    var lastLogged by remember { mutableStateOf("") }

    // Читаем настройки как состояния — при изменении ползунков рекоменпуется пересчёт.
    val minS = scaleService.minScale
    val maxS = scaleService.maxScale
    val factor = scaleService.manualFactor

    // Масштаб считается от измеренной доступной площади и текущей плотности:
    // при смене масштаба окна/плотности/настроек recomposition пересчитывает его сам.
    val scale = remember(availPx, density.density, minS, maxS, factor) {
        val availWdp = with(density) { availPx.width.toDp().value }
        val availHdp = with(density) { availPx.height.toDp().value }
        scaleService.scaleOf(availWdp, availHdp)
    }

    debug.fieldPx = Size(
        with(density) { availPx.width.toDp().value },
        with(density) { availPx.height.toDp().value },
    )
    debug.appliedScale = scale

    SideEffect {
        val line = "field dp=${with(density) { availPx.width.toDp().value.toInt() }}x" +
            "${with(density) { availPx.height.toDp().value.toInt() }} density=${density.density} scale=$scale"
        if (line != lastLogged) {
            lastLogged = line
            log.d(TAG, line)
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Отдельный полноразмерный измеритель СВОЕЙ доступной площади (не поля):
        // он заполняет контейнер и потому стабилен, без обратной связи от масштаба.
        Box(Modifier.fillMaxSize().onSizeChanged { availPx = it }) {}
        ScaledField(scale, Modifier.align(Alignment.Center)) { content() }
    }
}

@Composable
private fun ScaledField(scale: Float, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.size((Geo.W * scale).dp, (Geo.H * scale).dp)) {
        Box(
            Modifier
                .size(Geo.W.dp, Geo.H.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0f, 0f)
                },
            contentAlignment = Alignment.TopStart,
        ) { content() }
    }
}

/**
 * Слой отладки: рисует/скрывает маркеры углов прямоугольников (по формуле из GuiGeometry).
 */
object DebugCornersLayer {
    @Composable
    fun View(show: Boolean, modifier: Modifier = Modifier) {
        if (!show) return
        Canvas(modifier) { drawInDeviceSpace { draw() } }
    }

    fun DrawScope.draw() {
        val targets = listOf(
            Color.White to Geo.Red,
            Color.Yellow to Geo.Blue,
            Color.Black to Geo.White,
        )
        targets.forEach { (dot, g) ->
            g.corners.forEach { c ->
                drawCircle(Color(0xFF00E5FF), radius = 3f, center = c, style = Stroke(1f))
                drawCircle(dot, radius = 2f, center = c)
            }
        }
    }
}

private const val TAG = "TestDevice"
