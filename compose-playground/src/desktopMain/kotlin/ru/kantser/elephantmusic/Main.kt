package ru.kantser.elephantmusic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
            App()
        }
    }
}

/** Логирует геометрию окна и держит его в пределах активного монитора (опираясь на сервис метрик). */
@Composable
private fun ClampAndLogWindow(state: WindowState) {
    val log: AppLog = koinInject()
    val metrics: WindowMetricsProvider = koinInject()

    LaunchedEffect(Unit) {
        while (true) {
            runCatching {
                val disp = metrics.activeDisplay()
                if (disp != null) {
                    // Ширина/высота окна не должны превышать рабочий экран активного монитора.
                    // (px -> dp: логический размер dp = физический px / масштаб ОС.)
                    val availWdp = (disp.widthPx / disp.scaleFactor).dp
                    val availHdp = (disp.heightPx / disp.scaleFactor).dp
                    val curW = state.size.width
                    val curH = state.size.height
                    val newW = if (curW > availWdp) availWdp else curW
                    val newH = if (curH > availHdp) availHdp else curH
                    if (newW != curW || newH != curH) {
                        state.size = DpSize(newW, newH)
                        log.i(
                            TAG,
                            "окно обрезано до активного экрана: ${newW.value.toInt()}x${newH.value.toInt()}dp " +
                                "(monitor ${disp.name} ${disp.widthPx}x${disp.heightPx} scale=${disp.scaleFactor})",
                        )
                    }
                    log.d(
                        TAG,
                        "window ${curW.value.toInt()}x${curH.value.toInt()}dp " +
                            "monitor=${disp.name} ${disp.widthPx}x${disp.heightPx} scale=${disp.scaleFactor}",
                    )
                }
            }
            delay(2000)
        }
    }
}

private const val TAG = "Window"
