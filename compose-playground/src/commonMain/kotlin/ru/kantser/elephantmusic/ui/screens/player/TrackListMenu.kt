package ru.kantser.elephantmusic.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.kantser.elephantmusic.ui.screens.player.SvgGeometry as Geo
import ru.kantser.elephantmusic.ui.screens.player.SvgGradients as G

/**
 * Меню плей-листа — обычный Compose список по данным (state.tracks).
 * Только строки треков; без Canvas-геометрии.
 */
@Composable
internal fun TrackListMenu(
    state: ScreenState,
    onSelect: (Int) -> Unit,
) {
    val s = Geo.Screen
    val tracks = state.tracks
    val contentH = s.LightH - s.TopBarH
    if (tracks.isEmpty()) {
        Text(
            "Список пуст",
            color = G.MenuText, fontSize = 11.sp,
            modifier = Modifier
                .offset(x = s.LightX.dp, y = (s.LightY + s.TopBarH).dp)
                .padding(10.dp),
        )
        return
    }

    // Видимое окно списка: список прокручивается внутри экрана (state.listScroll).
    val start = state.listScroll.coerceIn(0, (tracks.size - VISIBLE_LIST_ROWS).coerceAtLeast(0))
    val end = minOf(start + VISIBLE_LIST_ROWS, tracks.size)

    Box(
        Modifier
            .offset(x = s.LightX.dp, y = (s.LightY + s.TopBarH).dp)
            .width(s.LightW.dp)
            .height(contentH.dp)
            .clipToBounds()
            .padding(horizontal = 6.dp),
    ) {
        Column {
            for (i in start until end) {
                val track = tracks[i]
                val selected = i == state.trackSel
                val interaction = remember(i) { MutableInteractionSource() }
                val hovered by interaction.collectIsHoveredAsState()
                val bg = when {
                    selected -> G.SelectedDark
                    hovered -> Color.White.copy(alpha = 0.18f)
                    else -> Color.Transparent
                }
                Text(
                    track.title,
                    color = if (selected) Color(0xFF0c2a3f) else G.MenuText,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(LIST_ROW_HEIGHT.dp)
                        .background(bg)
                        .hoverable(interaction)
                        .clickable(interactionSource = interaction, indication = null) { onSelect(i) },
                )
            }
        }
    }
}
