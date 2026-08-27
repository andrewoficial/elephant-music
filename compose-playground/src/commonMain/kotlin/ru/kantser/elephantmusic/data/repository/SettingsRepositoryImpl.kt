package ru.kantser.elephantmusic.data.repository

import ru.kantser.elephantmusic.domain.model.AppSettings
import ru.kantser.elephantmusic.domain.repository.SettingsRepository

class SettingsRepositoryImpl(private val store: JsonFileStore) : SettingsRepository {
    override fun load(): AppSettings = store.read("settings.json") ?: AppSettings()

    override fun save(settings: AppSettings) = store.write("settings.json", settings)
}
