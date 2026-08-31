package ru.kantser.elephantmusic.ui.screens.test.gui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.kantser.elephantmusic.ui.screens.test.gui.GuiGeometry as Geo

/**
 * Композиционный корень тестового поля (аналог PlayerDeviceView у Ritmix):
 * логический холст GuiGeometry.W x GuiGeometry.H, масштабируется под доступную площадь
 * через GuiScaleService (DeviceScale).
 *  - FIELD: красный → синий → белый → маркеры углов;
 *  - BODY:  реальный корпус плеера с экраном (RitmixBodyLayer).
 */
@Composable
fun TestDeviceView(
    mode: DebugViewMode,
    showCorners: Boolean,
    onToggleCorners: () -> Unit,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        DeviceScale { scaledContent(mode, showCorners, onToggleCorners) }
    }
}

@Composable
private fun scaledContent(
    mode: DebugViewMode,
    showCorners: Boolean,
    onToggleCorners: () -> Unit,
) {
    Box(
        Modifier
            .size(Geo.W.dp, Geo.H.dp)
            .clickable { onToggleCorners() },
        contentAlignment = Alignment.TopStart,
    ) {
        if (mode == DebugViewMode.BODY) {
            RitmixBodyLayer.View(showCorners, Modifier.matchParentSize())
        } else {
            RectLayerRed.View(Modifier.matchParentSize())
            RectLayerBlue.View(Modifier.matchParentSize())
            RectLayerWhite.View(Modifier.matchParentSize())
            DebugCornersLayer.View(showCorners, Modifier.matchParentSize())
        }
    }
}
