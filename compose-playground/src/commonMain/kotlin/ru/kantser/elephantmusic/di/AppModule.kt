package ru.kantser.elephantmusic.di

import org.koin.dsl.module
import ru.kantser.elephantmusic.data.repository.JsonFileStore
import ru.kantser.elephantmusic.data.repository.PlaylistRepositoryImpl
import ru.kantser.elephantmusic.data.repository.PlayerStateRepositoryImpl
import ru.kantser.elephantmusic.data.repository.SettingsRepositoryImpl
import ru.kantser.elephantmusic.data.service.LastFmService
import ru.kantser.elephantmusic.domain.controller.PlayerController
import ru.kantser.elephantmusic.domain.repository.PlaylistRepository
import ru.kantser.elephantmusic.domain.repository.PlayerStateRepository
import ru.kantser.elephantmusic.domain.repository.SettingsRepository
import ru.kantser.elephantmusic.platform.AppStorage
import ru.kantser.elephantmusic.platform.AudioPlayer
import ru.kantser.elephantmusic.platform.createAppStorage
import ru.kantser.elephantmusic.platform.createAudioPlayer
import ru.kantser.elephantmusic.platform.createHttpClient

val appModule = module {
    single<AppStorage> { createAppStorage() }
    single { JsonFileStore(get()) }
    single<PlaylistRepository> { PlaylistRepositoryImpl(get()) }
    single<PlayerStateRepository> { PlayerStateRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single { createHttpClient() }
    single { LastFmService(get(), get()) }
    single<AudioPlayer> { createAudioPlayer() }
    single { PlayerController(get(), get(), get(), get(), get()) }
}
