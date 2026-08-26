# Архитектура ElephantMusic

Этот документ описывает текущее устройство проекта и состояние разделения слоёв.
Сейчас в проекте **два GUI** — это нормальный промежуточный этап миграции:

| GUI | Технология | Где лежит | Портируется на Android? |
|-----|-----------|-----------|--------------------------|
| Десктопное приложение | JavaFX (Maven) | `src/main/java/ru/kantser/elephantmusic/**` | Нет |
| Новый UI | Compose Multiplatform (Gradle/Kotlin) | `compose-playground/**` | Да (Android/iOS/Desktop/Web) |

---

## 1. Текущее разделение слоёв (JavaFX-приложение)

Проект уже не монолит — слои выделены по пакетам, но границы местами «мягкие».

```
┌─────────────────────────────────────────────────────────┐
│ Presentation  (controller/, view/, *.fxml)              │
│   MainWindowController, PlayListPanelController, ...     │
│   view/dialog/, view/update/ (Stage-окна)               │
├─────────────────────────────────────────────────────────┤
│ Application / Services (service/)                        │
│   PlaylistService, AudioPlayerService, WindowTitleService│
│   service/tag, service/identification, service/lastfm,   │
│   service/update, service/webui                          │
├─────────────────────────────────────────────────────────┤
│ Data / Persistence (service/settings/)                   │
│   PlayListSaverService, SettingsService, PlayerStateService│
│   (+ Jackson-реализации, пишут в ~/.ElephantPlayer/*.json)│
├─────────────────────────────────────────────────────────┤
│ Domain / Model (model/)                                  │
│   Track, Playlist, AppSettings, PlayerState, TagFields, ...│
└─────────────────────────────────────────────────────────┘
```

Зависимости направлены «вниз»:
`controller → service → settings/model`, а `model` ни от чего не зависит.
Внедрение — через Guice (`AppModule`), FXML-контроллеры получают сервисы по `@Inject`.

## 2. Оценка: удовлетворительно (C)

Слои есть и в целом соблюдаются. Что хорошо:

- Модель (`model/`) — чистые POJO без логики и без зависимостей от UI/JSON.
- Персистенция вынесена за интерфейсы (`PlayListSaverService`, `SettingsService`),
  реализованы поверх Jackson — данные изолированы от бизнес-логики.
- Внешние интеграции (Last.fm, AudD/ACRCloud, обновления) лежат в отдельных пакетах.
- Новые сервисы (теги, распознавание) сделаны через интерфейсы + Guice Multibinder.

Что «провисает» (главные претензии, из-за которых не «четвёрка»):

1. **Толстые контроллеры.** `PlayListPanelController` делает слишком много:
   сканирование папок, чтение тегов (jaudiotagger), удаление файла с ретраями,
   построение меню и т.д. Это смешение Presentation + Domain + Data в одном классе.
2. **Сервисы местами смешивают бизнес и персистенцию.** `PlaylistService` одновременно
   управляет навигацией/индексами и сам вызывает `playListSaver.savePlayLists(...)`.
3. **Нет слоя use-case'ов.** Контроллеры дергают сервисы напрямую; нет тонких
   «команд» типа `PlayNextTrackUseCase`, что усложняет тестирование.
4. **Прямые JavaFX-зависимости в сервисах.** `PlaylistService`/`AudioPlayerService`
   используют `ObservableList`, `Platform`, `MediaPlayer` — то есть слой Application
   привязан к JavaFX (для миграции на Compose это стоит постепенно убирать).

## 3. Рекомендуемый план улучшений (постепенно, без риска)

1. Вынести из `PlayListPanelController`:
   - чтение метаданных трека → `service/tag/TrackMetadataService`;
   - сканирование папок → `service/tag/FolderScannerService`;
   - логику удаления файла → уже в `PlaylistService` (ок).
2. Разделить `PlaylistService`: оставить в нём доменную логику (плейлисты, навигация),
   а сохранение вызывать через тонкий `PlaylistRepository` (интерфейс уже есть —
   `PlayListSaverService`), сделав персистенцию явной, а не внутри каждого метода.
3. Ввести use-case-объекты (`PlayTrackUseCase`, `PausePlaybackUseCase` и т.п.) по мере
   роста сложности — сейчас можно отложить, «тройка» и так набирается.
4. Изолировать JavaFX-специфику в Presentation-слое, чтобы доменные/бизнес-сервисы
   можно было переиспользовать из Compose- и Web-UI.

> Важно: рефакторинг не обязателен для текущего этапа. Код рабочий и считываемый.
> Приоритет сейчас — дотянуть Compose-UI и Web-UI, а JavaFX-слой рефакторить потом.

## 4. Новые слои (Compose + Web)

- **Compose Multiplatform** (`compose-playground/`) — отдельный Gradle-модуль,
  самодостаточный, пока только демо. Подробный гайд — в `compose-playground/README.md`.
- **Web UI** (`service/webui/`) — встроенный HTTP-сервер (`jdk.httpserver`) + шаблон
  `resources/webui/index.html`. Это «пульт» в стиле KODI: набор команд поверх
  `AudioPlayerService`/`PlaylistService`. Слой Presentation, но с собственным
  транспортом (HTTP вместо JavaFX-событий).
