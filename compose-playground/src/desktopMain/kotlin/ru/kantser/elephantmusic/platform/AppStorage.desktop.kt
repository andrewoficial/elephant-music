package ru.kantser.elephantmusic.platform

import java.nio.file.Files
import java.nio.file.Path

class DesktopAppStorage : AppStorage {
    private val dir: Path = Path.of(System.getProperty("user.home"), ".ElephantPlayer")

    override fun dataDirectory(): String = dir.toString()

    override fun readFile(fileName: String): String? {
        val file = dir.resolve(fileName)
        return if (Files.exists(file)) Files.readString(file) else null
    }

    override fun writeFile(fileName: String, content: String) {
        Files.createDirectories(dir)
        Files.writeString(dir.resolve(fileName), content)
    }
}

actual fun createAppStorage(): AppStorage = DesktopAppStorage()
