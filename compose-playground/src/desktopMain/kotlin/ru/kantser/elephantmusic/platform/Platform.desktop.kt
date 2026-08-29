package ru.kantser.elephantmusic.platform

import java.awt.Desktop
import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.JFileChooser
import javax.swing.JFrame

actual fun openBrowser(url: String): Boolean = try {
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(URI(url))
        true
    } else {
        false
    }
} catch (e: Exception) {
    false
}

actual fun openFolder(path: String): Boolean = try {
    Files.createDirectories(Path.of(path))
    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(File(path))
        true
    } else {
        false
    }
} catch (e: Exception) {
    false
}

actual fun chooseFolder(title: String): String? {
    val parent = JFrame()
    parent.isVisible = false
    val chooser = JFileChooser()
    chooser.dialogTitle = title
    chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    val result = chooser.showDialog(parent, "Выбрать")
    parent.dispose()
    return if (result == JFileChooser.APPROVE_OPTION) chooser.selectedFile.absolutePath else null
}

actual fun deleteFileCompletely(path: String): Boolean = try {
    Files.deleteIfExists(Path.of(path))
} catch (e: Exception) {
    false
}

actual fun scanAudioFilesInFolder(folder: String): List<String> {
    val allowed = setOf("mp3", "wav", "flac", "ogg", "m4a", "aac")
    val root = Path.of(folder)
    if (!Files.isDirectory(root)) return emptyList()
    return try {
        Files.walk(root).use { stream ->
            stream.filter { Files.isRegularFile(it) }
                .map { it.toString() }
                .filter { it.substringAfterLast('.').lowercase() in allowed }
                .toList()
        }
    } catch (e: Exception) {
        emptyList()
    }
}

actual fun settingsFolderPath(): String = Path.of(System.getProperty("user.home"), ".ElephantPlayer").toAbsolutePath().toString()
