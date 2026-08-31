package ru.kantser.elephantmusic.ui.screens.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.kantser.elephantmusic.domain.model.Track
import ru.kantser.elephantmusic.ui.screens.player.SvgGeometry as Geo
import ru.kantser.elephantmusic.ui.screens.player.SvgGradients as G

/**
 * Контент экрана: светлый фон + синяя панель + статусбар (рисуется Canvas)
 * + подменю, которые уже обычный Compose UI по данным (меню/список/сейчас играет).
 * Геометрия — в локальных координатах рамки (0..332, 0..196).
 */
@Composable
fun ScreenSurface(
    state: ScreenState,
    isPlaying: Boolean,
    nowPlaying: Track?,
    nowPosition: Double,
    nowDuration: Double,
    onSelectHome: (Int) -> Unit,
    onSelectTrack: (Int) -> Unit,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    val s = Geo.Screen
    Box(
        Modifier
            .offset(x = 70.dp, y = 76.dp)
            .width(s.LocalW.dp)
            .height(s.LocalH.dp),
    ) {
        // Светлый фон + синяя панель
        Canvas(Modifier.width(s.LocalW.dp).height(s.LocalH.dp)) {
            drawRoundRect(
                G.ScreenBg,
                Offset(s.LightX, s.LightY),
                Size(s.LightW, s.LightH),
                CornerRadius(2f),
            )
            drawRoundRect(
                G.PanelBlue,
                Offset(s.PanelX, s.PanelY),
                Size(s.PanelW, s.PanelH),
                CornerRadius(2f),
            )
        }

        // Статусбар поверх светлой зоны
        StatusBar()

        // Синяя панель: лого-текст
        BluePanelTexts()

        // Подменю
        when (state.mode) {
            ScreenMode.HOME -> HomeMenu(state, onSelectHome)
            ScreenMode.LIST -> TrackListMenu(state, onSelectTrack)
            ScreenMode.NOW -> NowPlayingView(nowPlaying, isPlaying, nowPosition, nowDuration, onPlayPause, onPrev, onNext)
            ScreenMode.VIDEO, ScreenMode.PHOTO, ScreenMode.TEXT,
            ScreenMode.RECORD, ScreenMode.OTHER, ScreenMode.SETTINGS,
            ScreenMode.EMULATOR,
            -> PlaceholderView(state.mode)
        }
    }
}

@Composable
private fun StatusBar() {
    val s = Geo.Screen
    Box(
        Modifier
            .offset(x = s.LightX.dp, y = s.LightY.dp)
            .width(s.LightW.dp)
            .height(s.TopBarH.dp)
            .background(Color.White.copy(alpha = 0.18f)),
    ) {
        // Батарея справа
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp).height(s.TopBarH.dp)) {
            Spacer(Modifier.width(1.dp).weight(1f))
            Box(Modifier.width(16.dp).height(9.dp).clip(RoundedCornerShape(1.dp)).background(Color.White.copy(alpha = 0.9f)))
        }
    }
}

/** Синяя панель: лого-текст. Вызывается отдельно, если нужно отрисовать на панели. */
@Composable
internal fun BluePanelTexts() {
    val s = Geo.Screen
    Column(
        Modifier
            .offset(x = s.PanelX.dp, y = s.PanelY.dp)
            .width(s.PanelW.dp)
            .padding(horizontal = 6.dp)
            .height(s.PanelH.dp),
    ) {
        Spacer(Modifier.weight(1f))
        Text("MP4  VIDEO", color = G.SidePanelText.copy(alpha = 0.85f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text("RF-8800", color = G.SidePanelText.copy(alpha = 0.6f), fontSize = 8.sp)
        Spacer(Modifier.weight(1f))
    }
}
