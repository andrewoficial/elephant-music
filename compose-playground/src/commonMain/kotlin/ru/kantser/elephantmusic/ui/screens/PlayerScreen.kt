package ru.kantser.elephantmusic.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ru.kantser.elephantmusic.domain.controller.PlayerController

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
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Сейчас играет", style = MaterialTheme.typography.titleLarge)

        val track = s.currentTrack
        Text(
            text = if (track != null) "${track.artist} - ${track.title}" else "— трек не выбран —",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )

        Spacer(Modifier.height(24.dp))

        if (track != null) {
            val progressPercent = if (s.durationSeconds > 0) {
                (s.positionSeconds / s.durationSeconds * 100).toFloat().coerceIn(0f, 100f)
            } else 0f
            Slider(
                value = progressPercent,
                onValueChange = { controller.seek(it.toDouble()) },
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

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🔉")
            Spacer(Modifier.width(8.dp))
            Slider(
                value = s.volume.toFloat(),
                onValueChange = { controller.setVolume(it.toDouble()) },
                modifier = Modifier.width(220.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text("🔊")
        }
    }
}
