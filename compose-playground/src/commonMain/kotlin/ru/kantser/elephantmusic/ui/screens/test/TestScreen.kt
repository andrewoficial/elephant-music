package ru.kantser.elephantmusic.ui.screens.test

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import ru.kantser.elephantmusic.platform.AppLog
import ru.kantser.elephantmusic.ui.screens.player.DeviceButton
import ru.kantser.elephantmusic.ui.screens.player.HOME_ITEMS
import ru.kantser.elephantmusic.ui.screens.test.gui.DebugPanel
import ru.kantser.elephantmusic.ui.screens.test.gui.DebugViewMode
import ru.kantser.elephantmusic.ui.screens.test.gui.TestDeviceView

/**
 * Тестовая вкладка «Отладка SVG»: упрощённый аналог Ritmix-плеера.
 * Поле (красный/синий/белый) или чертёж корпуса плеера вписывается в доступную область
 * автоматически (GuiScaleService); снизу — отладочная панель (координаты углов, окно,
 * область, ползунки масштаба). Клик по полю — показать/скрыть маркеры углов.
 */
@Composable
fun TestScreen() {
    var showCorners by remember { mutableStateOf(true) }
    var mode by remember { mutableStateOf(DebugViewMode.BODY) }
    var menuIndex by remember { mutableStateOf(0) }
    val log: AppLog = koinInject()

    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            TestDeviceView(
                mode = mode,
                showCorners = showCorners,
                onToggleCorners = { showCorners = !showCorners },
                onButton = { btn ->
                    when (btn) {
                        DeviceButton.REWIND -> menuIndex = (menuIndex - 1).mod(HOME_ITEMS.size)
                        DeviceButton.FWD -> menuIndex = (menuIndex + 1).mod(HOME_ITEMS.size)
                        else -> log.i("DebugSVG", "btn pressed: ${btn.name}")
                    }
                },
                menuIndex = menuIndex,
            )
        }
        DebugPanel(
            mode = mode,
            onModeChange = { mode = it },
            showCorners = showCorners,
            onToggleCorners = { showCorners = !showCorners },
        )
    }
}
