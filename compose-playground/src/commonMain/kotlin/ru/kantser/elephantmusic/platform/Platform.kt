package ru.kantser.elephantmusic.platform

expect fun openBrowser(url: String): Boolean

expect fun openFolder(path: String): Boolean

expect fun chooseFolder(title: String): String?

expect fun deleteFileCompletely(path: String): Boolean

expect fun scanAudioFilesInFolder(folder: String): List<String>

expect fun settingsFolderPath(): String

expect fun platformRuntimeLabel(): String

expect fun javaVersionLabel(): String
