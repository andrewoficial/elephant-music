package ru.kantser.elephantmusic.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import ru.kantser.elephantmusic.service.lastfm.LastFmAuthService;
import ru.kantser.elephantmusic.service.lastfm.LastFmScrobblerService;
import ru.kantser.elephantmusic.service.settings.JacksonPlayListService;
import ru.kantser.elephantmusic.service.settings.JacksonSettingsService;
import ru.kantser.elephantmusic.service.settings.PlayListSaverService;
import ru.kantser.elephantmusic.service.settings.SettingsService;
import ru.kantser.elephantmusic.service.update.ApplicationInfoService;
import ru.kantser.elephantmusic.service.update.ApplicationInfoServiceImpl;
import ru.kantser.elephantmusic.service.update.UpdateService;
import ru.kantser.elephantmusic.service.update.UpdateServiceImpl;
import ru.kantser.elephantmusic.view.dialog.LastFmAuthDialog;
import ru.kantser.elephantmusic.view.update.UpdateWindow;


import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        // Базовые сервисы приложения
        bind(PlaylistService.class).in(Singleton.class);
        bind(AudioPlayerService.class).in(Singleton.class);
        bind(WindowTitleService.class).in(Singleton.class);
        bind(JacksonSettingsService.class).in(Singleton.class);

        // Сервис для работы с плейлистами - неленивая инициализация
        bind(JacksonPlayListService.class).asEagerSingleton();

        // Привязка интерфейсов к реализациям
        bind(SettingsService.class).to(JacksonSettingsService.class).in(Singleton.class);
        bind(PlayListSaverService.class).to(JacksonPlayListService.class).in(Singleton.class);

        // Сервисы для работы с Last.fm
        bind(LastFmAuthService.class).in(Singleton.class);
        bind(LastFmScrobblerService.class).in(Singleton.class);
        bind(LastFmAuthDialog.class).in(Singleton.class);

        // Сервисы обновления
        bind(UpdateService.class).to(UpdateServiceImpl.class).in(Singleton.class);
        bind(ApplicationInfoService.class).to(ApplicationInfoServiceImpl.class).in(Singleton.class);
        bind(UpdateWindow.class).in(Singleton.class);
    }

    /**
     * Предоставляет настроенный экземпляр ObjectMapper для JSON сериализации/десериализации.
     * Регистрирует модуль для работы с Java 8 Date/Time API (JSR-310), включая поддержку Duration.
     *
     * @return настроенный ObjectMapper
     */
    @Provides
    @Singleton
    public ObjectMapper provideObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Регистрируем модуль для поддержки Java 8 Date/Time API
        mapper.registerModule(new JavaTimeModule());


        // Дополнительные настройки при необходимости
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }
}