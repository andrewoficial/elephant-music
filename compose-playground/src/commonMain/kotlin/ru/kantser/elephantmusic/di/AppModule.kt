package ru.kantser.elephantmusic.di

import org.koin.dsl.module
import ru.kantser.elephantmusic.data.repository.JsonFileStore
import ru.kantser.elephantmusic.data.repository.PlaylistRepositoryImpl
import ru.kantser.elephantmusic.data.repository.PlayerStateRepositoryImpl
import ru.kantser.elephantmusic.data.repository.SettingsRepositoryImpl
import ru.kantser.elephantmusic.data.service.LastFmAuthService
import ru.kantser.elephantmusic.data.service.LastFmService
import ru.kantser.elephantmusic.domain.controller.PlayerController
import ru.kantser.elephantmusic.domain.repository.PlaylistRepository
import ru.kantser.elephantmusic.domain.repository.PlayerStateRepository
import ru.kantser.elephantmusic.domain.repository.SettingsRepository
import ru.kantser.elephantmusic.platform.AppLog
import ru.kantser.elephantmusic.platform.AppStorage
import ru.kantser.elephantmusic.platform.AudioPlayer
import ru.kantser.elephantmusic.platform.WindowMetricsProvider
import ru.kantser.elephantmusic.platform.createAppLog
import ru.kantser.elephantmusic.platform.createAppStorage
import ru.kantser.elephantmusic.platform.createAudioPlayer
import ru.kantser.elephantmusic.platform.createHttpClient
import ru.kantser.elephantmusic.platform.createWindowMetricsProvider
import ru.kantser.elephantmusic.ui.screens.test.gui.GuiDpiService
import ru.kantser.elephantmusic.ui.screens.test.gui.GuiDpiServiceImpl
import ru.kantser.elephantmusic.ui.screens.test.gui.GuiResolutionService
import ru.kantser.elephantmusic.ui.screens.test.gui.GuiResolutionServiceImpl
import ru.kantser.elephantmusic.ui.screens.test.gui.GuiScaleService
import ru.kantser.elephantmusic.ui.screens.test.gui.GuiScaleServiceImpl
import ru.kantser.elephantmusic.ui.screens.test.gui.GuiDebugState
import ru.kantser.elephantmusic.ui.screens.test.gui.ScaleObserver
import ru.kantser.elephantmusic.ui.screens.test.gui.ScaleObserverImpl

val appModule = module {
    single<AppLog> { createAppLog() }
    single<WindowMetricsProvider> { createWindowMetricsProvider() }
    single<GuiResolutionService> { GuiResolutionServiceImpl(get()) }
    single<GuiDpiService> { GuiDpiServiceImpl(get()) }
    single<GuiScaleService> { GuiScaleServiceImpl() }
    single { GuiDebugState() }
    single<ScaleObserver> { ScaleObserverImpl(get(), get()) }
    single<AppStorage> { createAppStorage() }
    single { JsonFileStore(get()) }
    single<PlaylistRepository> { PlaylistRepositoryImpl(get()) }
    single<PlayerStateRepository> { PlayerStateRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single { createHttpClient() }
    single { LastFmService(get(), get()) }
    single { LastFmAuthService(get(), get()) }
    single<AudioPlayer> { createAudioPlayer() }
    single { PlayerController(get(), get(), get(), get(), get()) }
}
