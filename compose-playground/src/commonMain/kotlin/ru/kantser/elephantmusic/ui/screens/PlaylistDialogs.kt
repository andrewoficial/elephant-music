package ru.kantser.elephantmusic.ui.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import ru.kantser.elephantmusic.domain.model.Playlist
import ru.kantser.elephantmusic.domain.model.Track

/** Все модальные диалоги экрана плейлистов (вынесены из PlaylistScreen для компактности). */
@Composable
internal fun PlaylistDialogs(
    showCreate: Boolean,
    createName: String,
    onCreateNameChange: (String) -> Unit,
    onCreate: () -> Unit,
    onDismissCreate: () -> Unit,
    renameTarget: Playlist?,
    renameName: String,
    onRenameNameChange: (String) -> Unit,
    onRename: () -> Unit,
    onDismissRename: () -> Unit,
    deleteTarget: Playlist?,
    onDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    trackDeleteConfirm: Track?,
    onDeleteTrack: () -> Unit,
    onDismissTrackDelete: () -> Unit,
) {
    if (showCreate) {
        AlertDialog(
            onDismissRequest = onDismissCreate,
            title = { Text("Новый плейлист") },
            text = {
                OutlinedTextField(
                    value = createName,
                    onValueChange = onCreateNameChange,
                    label = { Text("Название") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = onCreate) { Text("Создать") }
            },
            dismissButton = {
                TextButton(onClick = onDismissCreate) { Text("Отмена") }
            },
        )
    }

    renameTarget?.let { pl ->
        AlertDialog(
            onDismissRequest = onDismissRename,
            title = { Text("Переименовать «${pl.name}»") },
            text = {
                OutlinedTextField(
                    value = renameName,
                    onValueChange = onRenameNameChange,
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = onRename) { Text("ОК") }
            },
            dismissButton = {
                TextButton(onClick = onDismissRename) { Text("Отмена") }
            },
        )
    }

    deleteTarget?.let { pl ->
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("Удалить лист") },
            text = { Text("Удалить плейлист «${pl.name}»? Файлы на компьютере не трогаются.") },
            confirmButton = {
                TextButton(onClick = onDelete) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) { Text("Отмена") }
            },
        )
    }

    trackDeleteConfirm?.let { track ->
        AlertDialog(
            onDismissRequest = onDismissTrackDelete,
            title = { Text("Удаление файла") },
            text = { Text("Трек «${track.title}» будет удалён не только из плейлиста, но и с вашего компьютера. Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(onClick = onDeleteTrack) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = onDismissTrackDelete) { Text("Отмена") }
            },
        )
    }
}
