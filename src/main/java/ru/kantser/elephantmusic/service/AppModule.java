package ru.kantser.elephantmusic.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import ru.kantser.elephantmusic.service.identification.AcrCloudIdentificationProvider;
import ru.kantser.elephantmusic.service.identification.AcoustIdIdentificationProvider;
import ru.kantser.elephantmusic.service.identification.AudDIdentificationProvider;
import ru.kantser.elephantmusic.service.identification.TrackIdentificationProvider;
import ru.kantser.elephantmusic.service.lastfm.LastFmAuthService;
import ru.kantser.elephantmusic.service.lastfm.LastFmScrobblerService;
import ru.kantser.elephantmusic.service.settings.JacksonPlayListService;
import ru.kantser.elephantmusic.service.tag.TagService;
import ru.kantser.elephantmusic.service.settings.JacksonPlayerStateService;
import ru.kantser.elephantmusic.service.settings.JacksonSettingsService;
import ru.kantser.elephantmusic.service.settings.PlayListSaverService;
import ru.kantser.elephantmusic.service.settings.PlayerStateService;
import ru.kantser.elephantmusic.service.settings.SettingsService;
import ru.kantser.elephantmusic.service.update.ApplicationInfoService;
import ru.kantser.elephantmusic.service.update.ApplicationInfoServiceImpl;
import ru.kantser.elephantmusic.service.update.UpdateService;
import ru.kantser.elephantmusic.service.update.UpdateServiceImpl;
import ru.kantser.elephantmusic.view.dialog.LastFmAuthDialog;
import ru.kantser.elephantmusic.view.dialog.TagEditorDialog;
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
        bind(PlayerStateService.class).to(JacksonPlayerStateService.class).in(Singleton.class);

        // Сервисы для работы с Last.fm
        bind(LastFmAuthService.class).in(Singleton.class);
        bind(LastFmScrobblerService.class).in(Singleton.class);
        bind(LastFmAuthDialog.class).in(Singleton.class);

        // Сервисы работы с тегами и распознаванием треков
        bind(TagService.class).in(Singleton.class);
        bind(TagEditorDialog.class).in(Singleton.class);

        Multibinder<TrackIdentificationProvider> providerBinder =
                Multibinder.newSetBinder(binder(), TrackIdentificationProvider.class);
        providerBinder.addBinding().to(AudDIdentificationProvider.class);
        providerBinder.addBinding().to(AcrCloudIdentificationProvider.class);
        providerBinder.addBinding().to(AcoustIdIdentificationProvider.class);

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