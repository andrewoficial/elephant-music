package ru.kantser.elephantmusic.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.kantser.elephantmusic.domain.controller.PlayerController
import ru.kantser.elephantmusic.domain.model.Track
import ru.kantser.elephantmusic.platform.pickAudioFiles

@Composable
fun PlaylistScreen(controller: PlayerController) {
    val s = controller.state
    val playlist = s.playlists.firstOrNull { it.name == s.currentPlaylistName }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Плейлист — ${playlist?.name ?: ""}", style = MaterialTheme.typography.titleLarge)
            Button(onClick = { controller.addTracks(pickAudioFiles()) }) {
                Text("Добавить файлы")
            }
        }
        Spacer(Modifier.height(12.dp))

        if (playlist == null || playlist.tracks.isEmpty()) {
            Text(
                "Плейлист пуст. Добавьте аудиофайлы.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(playlist.tracks) { track: Track ->
                TrackRow(track, current = track.filePath == s.currentTrack?.filePath) {
                    controller.playTrack(track)
                }
            }
        }
    }
}

@Composable
private fun TrackRow(track: Track, current: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
    ) {
        Row(Modifier.padding(12.dp)) {
            Text(
                text = if (current) "▶ " else "",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
            )
            Column {
                Text(track.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    track.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
}
