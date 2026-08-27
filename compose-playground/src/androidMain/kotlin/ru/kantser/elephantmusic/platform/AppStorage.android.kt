package ru.kantser.elephantmusic.platform

import ru.kantser.elephantmusic.AppContextHolder
import java.io.File

class AndroidAppStorage : AppStorage {
    private val dir: File = File(AppContextHolder.context.filesDir, "data")

    override fun dataDirectory(): String = dir.absolutePath

    override fun readFile(fileName: String): String? {
        val file = File(dir, fileName)
        return if (file.exists()) file.readText() else null
    }

    override fun writeFile(fileName: String, content: String) {
        dir.mkdirs()
        File(dir, fileName).writeText(content)
    }
}

actual fun createAppStorage(): AppStorage = AndroidAppStorage()
