package ru.kantser.elephantmusic.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.kantser.elephantmusic.domain.model.Playlist
import ru.kantser.elephantmusic.domain.model.Track

/** Строка трека в списке плейлиста + меню действий (перенести/копировать/удалить). */
@Composable
internal fun TrackRow(
    track: Track,
    current: Boolean,
    currentPlaylistName: String,
    playlists: List<Playlist>,
    onPlay: () -> Unit,
    onDeleteFromPlaylist: () -> Unit,
    onDeleteFromPc: () -> Unit,
    onMove: (String) -> Unit,
    onCopy: (String) -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val targets = playlists.filter { it.name != currentPlaylistName }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() },
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (current) "▶ " else "",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
            )
            Column(Modifier.weight(1f)) {
                Text(track.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    track.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            Text(
                "⋮",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier
                    .clickable { menuOpen = true }
                    .padding(4.dp),
            )
        }
    }

    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
        DropdownMenuItem(
            text = { Text("Воспроизвести") },
            onClick = { menuOpen = false; onPlay() },
        )
        DropdownMenuItem(
            text = { Text("Удалить из плейлиста") },
            onClick = { menuOpen = false; onDeleteFromPlaylist() },
        )
        DropdownMenuItem(
            text = { Text("Удалить файл с компьютера") },
            onClick = { menuOpen = false; onDeleteFromPc() },
        )
        targets.forEach { pl ->
            DropdownMenuItem(
                text = { Text("Перенести в «${pl.name}»") },
                onClick = { menuOpen = false; onMove(pl.name) },
            )
        }
        targets.forEach { pl ->
            DropdownMenuItem(
                text = { Text("Копировать в «${pl.name}»") },
                onClick = { menuOpen = false; onCopy(pl.name) },
            )
        }
    }
}
