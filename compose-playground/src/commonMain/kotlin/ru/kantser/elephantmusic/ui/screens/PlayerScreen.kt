package ru.kantser.elephantmusic.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.kantser.elephantmusic.domain.controller.PlayerController
import ru.kantser.elephantmusic.ui.theme.AppOrange

/** Классический плеер: текущий трек с ID3-тегами, спектр, прогресс, транспорт и громкость. */
@Composable
fun PlayerScreen(controller: PlayerController) {
    val s = controller.state

    LaunchedEffect(s.isPlaying) {
        while (s.isPlaying) {
            controller.tick()
            delay(500)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val track = s.currentTrack

        Text("Сейчас играет", style = MaterialTheme.typography.titleLarge)

        if (track == null) {
            Text(
                "— трек не выбран —",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 16.dp),
            )
            Spacer(Modifier.height(24.dp))
        } else {
            Spacer(Modifier.height(12.dp))

            // 1. Название трека и исполнитель — всегда с переносом по словам и многоточием,
            //    чтобы длинный текст не вылезал за пределы экрана.
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = track.artist.ifEmpty { "—" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )

            // 2. Дополнительные ID3-теги (альбом, год, жанр).
            val meta = listOfNotNull(
                track.album?.takeIf { it.isNotBlank() },
                track.year?.takeIf { it.isNotBlank() },
                track.genre?.takeIf { it.isNotBlank() },
            ).joinToString("  •  ")
            if (meta.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // 3. Динамический «эквалайзер» — полосы зависят от реального уровня звука.
            Spacer(Modifier.height(20.dp))
            SpectrumBars(
                levels = s.levels,
                playing = s.isPlaying,
                modifier = Modifier.fillMaxWidth().height(96.dp),
            )

            Spacer(Modifier.height(20.dp))

            val progressPercent = if (s.durationSeconds > 0) {
                (s.positionSeconds / s.durationSeconds * 100).toFloat().coerceIn(0f, 100f)
            } else 0f
            Slider(
                value = progressPercent,
                onValueChange = { controller.seek(it.toDouble()) },
                // progressPercent в диапазоне 0..100, поэтому valueRange тоже 0..100 —
                // иначе (по умолчанию 0..1) ползунок за ~3 с «улетает» на 100%.
                valueRange = 0f..100f,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { controller.previous() }) { Text("⏮") }
            Button(
                onClick = { controller.playPause() },
                enabled = track != null,
            ) { Text(if (s.isPlaying) "⏸" else "▶") }
            Button(onClick = { controller.next() }) { Text("⏭") }
        }

        Spacer(Modifier.height(24.dp))

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("🔉")
            Spacer(Modifier.width(8.dp))
            Slider(
                value = s.volume.toFloat(),
                onValueChange = { controller.setVolume(it.toDouble()) },
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Text("🔊")
        }

        track?.lyrics?.takeIf { it.isNotBlank() }?.let { lyrics ->
            Spacer(Modifier.height(24.dp))
            Text("Текст", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                text = lyrics,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Полосы спектра: рисуются по уровням [levels] (0..1), при паузе «замирают». */
@Composable
private fun SpectrumBars(levels: List<Float>, playing: Boolean, modifier: Modifier = Modifier) {
    val barColor = AppOrange
    val idleColor = AppOrange.copy(alpha = 0.18f)
    val bg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Canvas(modifier.background(bg, RoundedCornerShape(8.dp))) {
        val count = if (levels.isNotEmpty()) levels.size else 16
        val gapPx = 3.dp.toPx()
        val barW = (size.width - gapPx * (count - 1)) / count
        val spacing = barW + gapPx
        for (i in 0 until count) {
            val raw = if (i < levels.size) levels[i] else 0f
            val h = if (playing) {
                (raw.coerceIn(0f, 1f) * size.height)
            } else {
                size.height * 0.04f
            }
            val x = i * spacing
            val y = size.height - h
            drawRoundRect(
                color = if (raw > 0.02f) barColor else idleColor,
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = Size(barW, h.coerceAtLeast(2f)),
                cornerRadius = CornerRadius(2.dp.toPx()),
            )
        }
    }
}
