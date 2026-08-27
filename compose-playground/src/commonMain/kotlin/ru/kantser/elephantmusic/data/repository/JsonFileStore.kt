package ru.kantser.elephantmusic.data.repository

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.kantser.elephantmusic.platform.AppStorage

class JsonFileStore(val storage: AppStorage) {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    inline fun <reified T> read(fileName: String): T? {
        val content = storage.readFile(fileName) ?: return null
        return try {
            json.decodeFromString<T>(content)
        } catch (e: Exception) {
            null
        }
    }

    inline fun <reified T> write(fileName: String, value: T) {
        storage.writeFile(fileName, json.encodeToString(value))
    }
}
