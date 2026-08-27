package ru.kantser.elephantmusic.domain.repository

import ru.kantser.elephantmusic.domain.model.AppSettings

interface SettingsRepository {
    fun load(): AppSettings
    fun save(settings: AppSettings)
}
