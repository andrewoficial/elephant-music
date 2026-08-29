package ru.kantser.elephantmusic.platform

import java.awt.FileDialog
import java.io.File
import javax.swing.JFrame

actual suspend fun pickAudioFiles(): List<String> {
    val parent = JFrame()
    parent.isVisible = false
    val dialog = FileDialog(parent, "Выберите аудиофайлы", FileDialog.LOAD)
    dialog.isMultipleMode = true
    dialog.isVisible = true
    parent.dispose()

    val allowed = setOf("mp3", "wav", "flac", "ogg")
    val files = dialog.files ?: emptyArray()
    return files.map { it.absolutePath }
        .filter { it.substringAfterLast('.').lowercase() in allowed }

}
