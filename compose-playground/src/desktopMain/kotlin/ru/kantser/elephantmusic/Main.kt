package ru.kantser.elephantmusic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.delay
import org.koin.core.context.startKoin
import org.koin.compose.koinInject
import ru.kantser.elephantmusic.di.appModule
import ru.kantser.elephantmusic.platform.AppLog
import ru.kantser.elephantmusic.platform.WindowMetricsProvider
import ru.kantser.elephantmusic.ui.App
import ru.kantser.elephantmusic.ui.screens.test.gui.GuiDebugState

fun main() {
    startKoin { modules(appModule) }
    application {
        val state: WindowState = rememberWindowState(size = DpSize(1120.dp, 780.dp))
        Window(
            onCloseRequest = ::exitApplication,
            title = "ElephantMusic",
            state = state,
        ) {
            ClampAndLogWindow(state)
            TrackWindowSize(state)
            App()
        }
    }
}

/** Writes current window size (dp) to GuiDebugState and logs every size change. */
@Composable
private fun TrackWindowSize(state: WindowState) {
    val debug: GuiDebugState = koinInject()
    val log: AppLog = koinInject()
    var last by remember { mutableStateOf<DpSize?>(null) }
    LaunchedEffect(state.size) {
        val s = state.size
        debug.windowSize = s
        if (last != null && last != s) {
            log.i("WindowResize", "window size: ${last!!.width.value.toInt()}x${last!!.height.value.toInt()}dp -> " +
                "${s.width.value.toInt()}x${s.height.value.toInt()}dp")
        }
        last = s
    }
}

/**
 * Keeps the window within the active monitor's working area and logs the geometry in detail.
 *
 * IMPORTANT unit note: on Windows with display scaling (e.g. 175%), AWT reports DISPLAY BOUNDS
 * as LOGICAL (scaled) units, NOT physical pixels. For a 2560x1440 @ 175% monitor AWT reports
 * 1463x823 (= 2560/1.75). Compose [state.size] is also in dp at the window's density (1.75).
 * So AWT logical bounds ALREADY equal the dp limit — we must NOT divide by density again,
 * otherwise the limit becomes ~1.75x too small and the clamp fights manual resizing.
 */
@Composable
private fun ClampAndLogWindow(state: WindowState) {
    val log: AppLog = koinInject()
    val metrics: WindowMetricsProvider = koinInject()
    val density = LocalDensity.current.density

    // Track previously logged display to dump detailed info once per display change.
    var lastLogKey by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            runCatching {
                val disp = metrics.activeDisplay()
                if (disp != null) {
                    // Limit = AWT logical bounds = dp limit at the window's density.
                    val limitWdp = disp.widthPx.dp
                    val limitHdp = disp.heightPx.dp
                    val curW = state.size.width
                    val curH = state.size.height
                    val newW = if (curW > limitWdp) limitWdp else curW
                    val newH = if (curH > limitHdp) limitHdp else curH

                    // Physical estimate for cross-checking: logical * OS scale.
                    val physW = (disp.widthPx * disp.scaleFactor).toInt()
                    val physH = (disp.heightPx * disp.scaleFactor).toInt()
                    val curPxW = (curW.value * density).toInt()
                    val curPxH = (curH.value * density).toInt()

                    // Detailed dump once per display (at startup and whenever display changes).
                    val key = "${disp.id}|${disp.widthPx}x${disp.heightPx}x${disp.scaleFactor}"
                    if (key != lastLogKey) {
                        lastLogKey = key
                        log.i(
                            TAG,
                            "display ${disp.name}: physical≈${physW}x${physH}px, " +
                                "logical(AWT)=${disp.widthPx}x${disp.heightPx}, OS scale=${disp.scaleFactor}, " +
                                "compose density=$density",
                        )
                        log.i(
                            TAG,
                            "window ${curW.value.toInt()}x${curH.value.toInt()}dp [px $curPxW x $curPxH], " +
                                "limit=${limitWdp.value.toInt()}x${limitHdp.value.toInt()}dp",
                        )
                    }

                    val action = if (newW != curW || newH != curH) "CLAMP" else "NONE"

                    log.d(
                        TAG,
                        "eval window=${curW.value.toInt()}x${curH.value.toInt()}dp [px $curPxW x $curPxH] " +
                            "screen=${disp.widthPx}x${disp.heightPx} logical scaleFactor=${disp.scaleFactor} " +
                            "density=$density limit=${limitWdp.value.toInt()}x${limitHdp.value.toInt()}dp action=$action",
                    )

                    if (action != "NONE") {
                        state.size = DpSize(newW, newH)
                        log.i(TAG, "CLAMP window to ${newW.value.toInt()}x${newH.value.toInt()}dp " +
                            "(window ${curW.value.toInt()}x${curH.value.toInt()}dp exceeded logical screen " +
                            "${disp.widthPx}x${disp.heightPx}dp)")
                    }
                }
            }
            delay(2000)
        }
    }
}

private const val TAG = "Window"
