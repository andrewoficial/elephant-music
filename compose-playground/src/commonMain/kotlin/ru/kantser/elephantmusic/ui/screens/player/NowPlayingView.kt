package ru.kantser.elephantmusic.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.kantser.elephantmusic.domain.model.Track
import ru.kantser.elephantmusic.ui.screens.player.SvgGeometry as Geo
import ru.kantser.elephantmusic.ui.screens.player.SvgGradients as G

/**
 * «Сейчас играет» — обычный Compose UI: статус, название, артист, прогресс, управление.
 * Позиционируется в светлой зоне экрана.
 */
@Composable
internal fun NowPlayingView(
    track: Track?,
    isPlaying: Boolean,
    positionSeconds: Double,
    durationSeconds: Double,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    val s = Geo.Screen
    val contentH = s.LightH - s.TopBarH
    val progress = if (durationSeconds > 0) {
        (positionSeconds / durationSeconds).toFloat().coerceIn(0f, 1f)
    } else 0f
    Box(
        Modifier
            .offset(x = s.LightX.dp, y = (s.LightY + s.TopBarH).dp)
            .width(s.LightW.dp)
            .height(contentH.dp)
            .clipToBounds()
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Column(Modifier.fillMaxSize()) {
            Text(
                if (isPlaying) "PLAY ▶" else "PAUSE ❚❚",
                color = Color(0xFF165d3a),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )

            Text(
                track?.title ?: "—",
                color = G.MenuText, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Text(
                track?.artist ?: "", color = G.MenuText, fontSize = 10.sp, maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.weight(1f))

            // Прогресс воспроизведения (реальные позиция/длительность)
            Box(
                Modifier
                    .width(180.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.Black.copy(alpha = 0.2f)),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF0a3a70)),
                )
            }

            // Время: прошедшее / общее
            Text(
                text = "${fmtTime(positionSeconds)} / ${fmtTime(durationSeconds)}",
                color = G.MenuText.copy(alpha = 0.7f),
                fontSize = 9.sp,
                modifier = Modifier.padding(top = 2.dp),
            )

            Spacer(Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                NowPlayingButton("⏮", onPrev)
                Spacer(Modifier.width(12.dp))
                NowPlayingButton(if (isPlaying) "❚❚" else "▶", onPlayPause)
                Spacer(Modifier.width(12.dp))
                NowPlayingButton("⏭", onNext)
            }
        }
    }
}

@Composable
private fun NowPlayingButton(label: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    Box(
        Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(if (hovered) Color.White.copy(alpha = 0.25f) else Color.Transparent)
            .hoverable(interaction)
            .clickable(interactionSource = interaction, indication = null) { onClick() }
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = G.MenuText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

private fun fmtTime(seconds: Double): String {
    val total = seconds.toInt().coerceAtLeast(0)
    val m = total / 60
    val s = total % 60
    return "%02d:%02d".format(m, s)
}
