package ru.kantser.elephantmusic.ui.screens.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import ru.kantser.elephantmusic.domain.model.Track
import ru.kantser.elephantmusic.platform.AppLog
import ru.kantser.elephantmusic.platform.WindowMetricsProvider
import ru.kantser.elephantmusic.ui.screens.test.gui.GuiDebugState
import ru.kantser.elephantmusic.ui.screens.player.SvgGeometry as Geo

/**
 * Собирает весь прибор в логический холст 520x350 и масштабирует его под доступную площадь.
 * Слои снизу вверх: корпус → логотип → рамка → экран(меню) → кнопки.
 */
@Composable
fun PlayerDeviceView(
    state: ScreenState,
    isPlaying: Boolean,
    nowPlaying: Track?,
    nowPosition: Double,
    nowDuration: Double,
    onRewind: () -> Unit,
    onFwd: () -> Unit,
    onPlayPause: () -> Unit,
    onMenu: () -> Unit,
    onSelectHome: (Int) -> Unit,
    onSelectTrack: (Int) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val debug: GuiDebugState = koinInject()
    val size = Geo.W.dp
    DeviceScale {
        Box(Modifier.size(size, Geo.H.dp)) {
            PlayerBody.View(Modifier.fillMaxSize())
            RitmixLogo.View(Modifier.fillMaxSize())
            PlayerFrame.View(Modifier.fillMaxSize())
            ScreenSurface(
                state = state,
                isPlaying = isPlaying,
                nowPlaying = nowPlaying,
                nowPosition = nowPosition,
                nowDuration = nowDuration,
                onSelectHome = onSelectHome,
                onSelectTrack = onSelectTrack,
                onPlayPause = onPlayPause,
                onPrev = onPrevious,
                onNext = onNext,
            )
            ButtonRail(
                modifier = Modifier.fillMaxSize(),
                onRewind = onRewind,
                onMenu = onMenu,
                onPlayPause = onPlayPause,
                onFwd = onFwd,
            )
            if (debug.showPlayerBody) {
                Canvas(Modifier.fillMaxSize()) { drawInDeviceSpace { drawBodyBlueprint() } }
            }
        }
    }
}

@Composable
private fun DeviceScale(content: @Composable () -> Unit) {
    val log: AppLog = koinInject()
    val metrics: WindowMetricsProvider = koinInject()
    val density = LocalDensity.current

    var availPx by remember { mutableStateOf(IntSize.Zero) }
    var scale by remember { mutableFloatStateOf(1f) }
    var lastLogged by remember {
        mutableStateOf("")
    }
    var lastMonitor by remember { mutableStateOf<String?>(null) }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .align(Alignment.Center)
                .onSizeChanged { px ->
                    if (px == availPx) return@onSizeChanged
                    availPx = px

                    // Считаем масштаб ПОЛНОСТЬЮ в dp: px -> dp через плотность.
                    // Так положение элементов (в dp) не "разъезжается" при смене монитора/масштаба ОС.
                    val availWdp = with(density) { px.width.toDp().value }
                    val availHdp = with(density) { px.height.toDp().value }
                    val newScale = minOf(
                        if (availWdp > 0) availWdp / Geo.W else 1f,
                        if (availHdp > 0) availHdp / Geo.H else 1f,
                    ).coerceIn(0.1f, 4f)
                    scale = newScale

                    // Логирование (для отладки позиционирования).
                    val monitor = try {
                        metrics.activeDisplay()?.let { "${it.name} ${it.widthPx}x${it.heightPx} dpi=${it.densityDpi}" }
                    } catch (e: Exception) {
                        null
                    }
                    val dpi = with(density) { density.density * 160f }
                    val line = "window px=${px.width}x${px.height} dp=${availWdp.toInt()}x${availHdp.toInt()} " +
                        "densityDpi=${dpi.toInt()} deviceScale=$newScale monitor=$monitor"
                    if (line != lastLogged) {
                        lastLogged = line
                        if (monitor != null && monitor != lastMonitor) {
                            lastMonitor = monitor
                            log.i(TAG, "экран переключён -> $monitor")
                        }
                        log.d(TAG, line)
                    }
                },
        ) {
            ScaledDevice(scale) { content() }
        }
    }
}

@Composable
private fun ScaledDevice(scale: Float, content: @Composable () -> Unit) {
    val gs by animateFloatAsState(scale, label = "deviceScale")
    // Наружный бокс имеет размер прибора, умноженный на масштаб, — не оставляет лишнего
    // места при уменьшении и не вылезает за пределы окна.
    Box(Modifier.size((Geo.W * gs).dp, (Geo.H * gs).dp)) {
        Box(
            Modifier
                .size(Geo.W.dp, Geo.H.dp)
                .graphicsLayer {
                    scaleX = gs
                    scaleY = gs
                    transformOrigin = TransformOrigin(0f, 0f)
                },
            contentAlignment = Alignment.TopStart,
        ) { content() }
    }
}

private const val TAG = "PlayerDevice"
