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
import ru.kantser.elephantmusic.ui.screens.test.gui.DebugPanel
import ru.kantser.elephantmusic.ui.screens.test.gui.TestDeviceView

/**
 * Тестовая вкладка «Отладка SVG»: упрощённый аналог Ritmix-плеера.
 * Поле (красный/синий/белый) вписывается в доступную область автоматически (GuiScaleService);
 * снизу — отладочная панель (координаты углов, окно, область, ползунки масштаба).
 * Клик по полю — показать/скрыть маркеры углов.
 */
@Composable
fun TestScreen() {
    var showCorners by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            TestDeviceView(
                showCorners = showCorners,
                onToggleCorners = { showCorners = !showCorners },
            )
        }
        DebugPanel(
            showCorners = showCorners,
            onToggleCorners = { showCorners = !showCorners },
        )
    }
}
