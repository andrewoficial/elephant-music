package ru.kantser.elephantmusic.platform

import android.content.Intent
import android.net.Uri
import ru.kantser.elephantmusic.AppContextHolder

actual fun openBrowser(url: String): Boolean = try {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    AppContextHolder.context.startActivity(intent)
    true
} catch (e: Exception) {
    false
}

actual fun openFolder(path: String): Boolean = false

actual fun chooseFolder(title: String): String? = null

actual fun deleteFileCompletely(path: String): Boolean = false

actual fun scanAudioFilesInFolder(folder: String): List<String> = emptyList()

actual fun settingsFolderPath(): String =
    AppContextHolder.context.getExternalFilesDir(null)?.absolutePath
        ?: AppContextHolder.context.filesDir.absolutePath

actual fun platformRuntimeLabel(): String = "android"

actual fun javaVersionLabel(): String =
    System.getProperty("java.version") ?: "n/a"
