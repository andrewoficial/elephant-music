package ru.kantser.elephantmusic.service;

import com.google.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kantser.elephantmusic.model.Playlist;
import ru.kantser.elephantmusic.model.Track;
import ru.kantser.elephantmusic.service.settings.PlayListSaverService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaylistService {
    @Inject
    private PlayListSaverService playListSaver;

    private final ObservableList<Playlist> playlists = FXCollections.observableArrayList();

    @Getter
    private Playlist currentPlaylist;

    private final Map<Playlist, Integer> currentIndexes = new HashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(PlaylistService.class);

    @Inject
    public void initialize() {
        logger.info("Инициализирую сервис плейлиста");
        try {
            playlists.setAll(playListSaver.loadPlayLists());
        } catch (IOException e) {
            logger.warn("Ошибка чтения плейлистов при запуске {}", e.getMessage());
        }
        if (playlists.isEmpty()) {
            playlists.add(playListSaver.getDefaultPlayList());
        }
        currentPlaylist = playlists.get(0);
    }

    public ObservableList<Playlist> getPlaylists() {
        return playlists;
    }

    public void setCurrentPlaylist(Playlist playlist) {
        if (playlist != null && playlists.contains(playlist)) {
            this.currentPlaylist = playlist;
        }
    }

    public Playlist createPlaylist(String name) {
        Playlist playlist = new Playlist(name);
        playlists.add(playlist);
        currentIndexes.put(playlist, -1);
        updateFile("Создание плейлиста");
        return playlist;
    }

    public void addTrack(Track track) {
        currentPlaylist.addTrack(track);
        logger.info("Добавлен трек, теперь в коллекции {}", currentPlaylist.getTracks().size());
        updateFile("Добавление трека");
    }

    public void addTracks(Playlist playlist, List<Track> tracks) {
        playlist.getTracks().addAll(tracks);
        updateFile("Добавление треков");
    }

    public void removeTrack(Track track) {
        removeTrackFrom(currentPlaylist, track);
        updateFile("Удаление трека");
    }

    public boolean deleteFileCompletely(Track track) {
        Path path = track.getFilePath();
        logger.info("Попытка удаления файла: {}", path);
        for (int attempt = 1; attempt <= 10; attempt++) {
            logger.info("Попытка удаления {}/10", attempt);
            try {
                boolean deleted = Files.deleteIfExists(path);
                if (deleted) {
                    logger.info("Файл удалён с компьютера: {}", path);
                    return true;
                }
                logger.warn("Файл не найден на компьютере: {}", path);
                return false;
            } catch (IOException e) {
                logger.warn("Попытка {} не удалась, файл занят: {}", attempt, e.getMessage());
                if (attempt == 10) {
                    break;
                }
                System.gc();
                try {
                    Thread.sleep(250);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        logger.info("Проверка существования фактического после попыток: {}",
                Files.exists(path) ? "файл всё ещё существует" : "файл не существует");
        logger.error("Не удалось удалить файл трека с компьютера: {}", path);
        return false;
    }

    public boolean isFileLocked(Path path) {
        if (!Files.exists(path)) {
            return false;
        }
        Path probe = path.resolveSibling(path.getFileName().toString() + ".deletetest");
        try {
            Files.move(path, probe, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            return true;
        }
        try {
            Files.move(probe, path, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            logger.warn("Не удалось вернуть файл после проверки занятости: {}", probe, e);
        }
        return false;
    }

    public void moveTrack(Track track, Playlist target) {
        if (target == null || target == currentPlaylist) {
            return;
        }
        removeTrackFrom(currentPlaylist, track);
        target.addTrack(track);
        updateFile("Перенос трека");
    }

    public void copyTrack(Track track, Playlist target) {
        if (target == null) {
            return;
        }
        target.addTrack(track);
        updateFile("Копирование трека");
    }

    private void removeTrackFrom(Playlist playlist, Track track) {
        int index = playlist.getTracks().indexOf(track);
        playlist.removeTrack(track);

        int currentIndex = currentIndexes.getOrDefault(playlist, -1);
        if (index != -1 && index < currentIndex) {
            currentIndexes.put(playlist, currentIndex - 1);
        }
    }

    public void clearPlaylist() {
        currentPlaylist.clear();
        currentIndexes.put(currentPlaylist, -1);
        updateFile("Очистка листа");
    }

    public ObservableList<Track> getTracks() {
        return currentPlaylist.getTracks();
    }

    public Track getReplacementTrack(Track track) {
        ObservableList<Track> tracks = currentPlaylist.getTracks();
        int index = tracks.indexOf(track);
        if (index < 0) {
            return null;
        }
        if (index > 0) {
            return tracks.get(index - 1);
        }
        if (index == 0 && tracks.size() > 1) {
            return tracks.get(1);
        }
        return null;
    }

    public Track getNextTrack() {
        if (currentPlaylist.getTracks().isEmpty()) {
            return null;
        }

        int index = currentIndexes.getOrDefault(currentPlaylist, -1);
        if (index < currentPlaylist.getTracks().size() - 1) {
            index++;
        } else {
            index = 0;
        }
        currentIndexes.put(currentPlaylist, index);
        return currentPlaylist.getTracks().get(index);
    }

    public Track getPreviousTrack() {
        if (currentPlaylist.getTracks().isEmpty()) {
            return null;
        }

        int index = currentIndexes.getOrDefault(currentPlaylist, -1);
        if (index > 0) {
            index--;
        } else {
            index = currentPlaylist.getTracks().size() - 1;
        }
        currentIndexes.put(currentPlaylist, index);
        return currentPlaylist.getTracks().get(index);
    }

    public void setCurrentTrack(Track track) {
        currentIndexes.put(currentPlaylist, currentPlaylist.getTracks().indexOf(track));
    }

    public void persist() {
        updateFile("Сохранение плейлистов");
    }

    private void updateFile(String debugComment) {
        try {
            playListSaver.savePlayLists(playlists);
        } catch (IOException e) {
            logger.warn("Не удалось обновить файл плейлиста {} при отладочном комментарии [{}]", e.getMessage(), debugComment);
        }
    }
}
