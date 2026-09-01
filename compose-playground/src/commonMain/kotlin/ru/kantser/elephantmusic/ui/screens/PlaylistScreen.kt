package ru.kantser.elephantmusic.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.kantser.elephantmusic.domain.controller.PlayerController
import ru.kantser.elephantmusic.domain.controller.addTracks
import ru.kantser.elephantmusic.domain.controller.addTracksToCurrentPlaylist
import ru.kantser.elephantmusic.domain.controller.addTracksToPlaylist
import ru.kantser.elephantmusic.domain.controller.copyTrack
import ru.kantser.elephantmusic.domain.controller.createPlaylist
import ru.kantser.elephantmusic.domain.controller.deleteFileAndRemove
import ru.kantser.elephantmusic.domain.controller.deletePlaylist
import ru.kantser.elephantmusic.domain.controller.moveTrack
import ru.kantser.elephantmusic.domain.controller.removeTrackFromCurrent
import ru.kantser.elephantmusic.domain.controller.renamePlaylist
import ru.kantser.elephantmusic.domain.controller.selectPlaylist
import ru.kantser.elephantmusic.domain.model.Playlist
import ru.kantser.elephantmusic.domain.model.Track
import ru.kantser.elephantmusic.platform.pickAudioFiles
import ru.kantser.elephantmusic.platform.pickAudioFolder
import ru.kantser.elephantmusic.ui.theme.AppOrange
import kotlinx.coroutines.launch

@Composable
fun PlaylistScreen(controller: PlayerController) {
    val s = controller.state
    val playlists = s.playlists
    val current = playlists.firstOrNull { it.name == s.currentPlaylistName }
    val selectedTrackName = s.currentTrack?.filePath
    val scope = rememberCoroutineScope()

    var showCreate by remember { mutableStateOf(false) }
    var createName by remember { mutableStateOf("") }
    var renameTarget by remember { mutableStateOf<Playlist?>(null) }
    var renameName by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<Playlist?>(null) }
    var trackDeleteConfirm by remember { mutableStateOf<Track?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth()) {
            Text("Плейлисты", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            HeaderButtons(
                onAddFiles = { scope.launch { controller.addTracks(pickAudioFiles()) } },
                onAddFolder = {
                    scope.launch {
                        val files = pickAudioFolder()
                        if (files.isNotEmpty()) {
                            val first = files.first()
                            val parent = first.substringBeforeLast('/').substringBeforeLast('\\')
                            val folderName = if (!first.startsWith("content://")) {
                                parent.substringAfterLast('\\').substringAfterLast('/')
                            } else ""
                            val plName = folderName.ifBlank { "Аудио" }
                            if (controller.createPlaylist(plName)) {
                                controller.addTracksToCurrentPlaylist(files)
                            } else {
                                controller.addTracksToPlaylist(files, plName)
                            }
                            message = "Добавлено треков: ${files.size}"
                        } else {
                            message = "В папке не найдено аудиофайлов."
                        }
                    }
                },
                onNew = { showCreate = true },
            )
        }

        Spacer(Modifier.height(8.dp))

        PlaylistChips(
            playlists = playlists,
            currentPlaylistName = s.currentPlaylistName,
            onSelect = { controller.selectPlaylist(it) },
            onRename = { pl -> renameTarget = pl; renameName = pl.name },
            onDelete = { pl -> deleteTarget = pl },
        )

        Spacer(Modifier.height(8.dp))

        if (current == null || current.tracks.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                Text(
                    "Плейлист пуст. Добавьте аудиофайлы.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(current.tracks) { track ->
                    TrackRow(
                        track = track,
                        current = track.filePath == selectedTrackName,
                        currentPlaylistName = s.currentPlaylistName,
                        playlists = playlists,
                        onPlay = { controller.playTrack(track) },
                        onDeleteFromPlaylist = { controller.removeTrackFromCurrent(track) },
                        onDeleteFromPc = { trackDeleteConfirm = track },
                        onMove = { target -> controller.moveTrack(track, target) },
                        onCopy = { target -> controller.copyTrack(track, target) },
                    )
                }
            }
        }

        message?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }

    PlaylistDialogs(
        showCreate = showCreate,
        createName = createName,
        onCreateNameChange = { createName = it },
        onCreate = {
            if (controller.createPlaylist(createName)) {
                showCreate = false
                createName = ""
            }
        },
        onDismissCreate = { showCreate = false },
        renameTarget = renameTarget,
        renameName = renameName,
        onRenameNameChange = { renameName = it },
        onRename = {
            renameTarget?.let { controller.renamePlaylist(it.name, renameName) }
            renameTarget = null
        },
        onDismissRename = { renameTarget = null },
        deleteTarget = deleteTarget,
        onDelete = {
            deleteTarget?.let { controller.deletePlaylist(it.name) }
            deleteTarget = null
        },
        onDismissDelete = { deleteTarget = null },
        trackDeleteConfirm = trackDeleteConfirm,
        onDeleteTrack = {
            trackDeleteConfirm?.let { track ->
                message = if (controller.deleteFileAndRemove(track)) "Файл удалён." else "Трек удалён из плейлиста, но файл не найден."
            }
            trackDeleteConfirm = null
        },
        onDismissTrackDelete = { trackDeleteConfirm = null },
    )
}

/** Кнопки добавления в шапку (FlowRow — переносятся, а не прячутся на узких экранах). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeaderButtons(
    onAddFiles: () -> Unit,
    onAddFolder: () -> Unit,
    onNew: () -> Unit,
) {
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(onClick = onAddFiles) { Text("Добавить файлы") }
        OutlinedButton(onClick = onAddFolder) { Text("Добавить папку") }
        Button(onClick = onNew) { Text("Новый") }
    }
}

/** Горизонтально-прокручиваемые «чипы» плейлистов. */
@Composable
private fun PlaylistChips(
    playlists: List<Playlist>,
    currentPlaylistName: String,
    onSelect: (String) -> Unit,
    onRename: (Playlist) -> Unit,
    onDelete: (Playlist) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        playlists.forEach { pl ->
            val selected = pl.name == currentPlaylistName
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onSelect(pl.name) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    pl.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) AppOrange else MaterialTheme.colorScheme.onSurface,
                )
                if (selected) {
                    var open by remember { mutableStateOf(false) }
                    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                        DropdownMenuItem(
                            text = { Text("Переименовать") },
                            onClick = { open = false; onRename(pl) },
                        )
                        DropdownMenuItem(
                            text = { Text("Удалить лист") },
                            onClick = { open = false; onDelete(pl) },
                        )
                    }
                    Text(
                        " ⋯",
                        color = AppOrange,
                        modifier = Modifier
                            .clickable { open = true }
                            .padding(start = 4.dp),
                    )
                }
            }
        }
    }
}
