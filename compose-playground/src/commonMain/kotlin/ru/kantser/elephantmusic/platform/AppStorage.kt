package ru.kantser.elephantmusic.platform

interface AppStorage {
    fun dataDirectory(): String
    fun readFile(fileName: String): String?
    fun writeFile(fileName: String, content: String)
}

expect fun createAppStorage(): AppStorage
