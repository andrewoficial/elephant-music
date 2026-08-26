package ru.kantser.elephantmusic.service.settings;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kantser.elephantmusic.model.Playlist;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class JacksonPlayListService implements PlayListSaverService {
    private static final Logger logger = LoggerFactory.getLogger(JacksonPlayListService.class);

    private final ObjectMapper objectMapper;
    private final Path playListPath;

    @Inject
    public JacksonPlayListService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.playListPath = Paths.get(System.getProperty("user.home"), ".ElephantPlayer", "playlist.json");
    }

    @Override
    public List<Playlist> loadPlayLists() throws IOException {
        if (!Files.exists(playListPath)) {
            logger.warn("Ошибка доступа к папке, возвращаю пустой плейлист.");
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(playListPath.toFile(), new TypeReference<List<Playlist>>() {});
        } catch (IOException listError) {
            // Миграция со старого формата: одиночный объект Playlist
            logger.info("Файл плейлиста в старом формате, выполняю миграцию: {}", listError.getMessage());
            try {
                Playlist legacy = objectMapper.readValue(playListPath.toFile(), Playlist.class);
                List<Playlist> migrated = new ArrayList<>();
                migrated.add(legacy);
                return migrated;
            } catch (IOException legacyError) {
                logger.warn("Не удалось прочитать файл плейлиста, возвращаю пустой список: {}", legacyError.getMessage());
                return new ArrayList<>();
            }
        }
    }

    @Override
    public void savePlayLists(List<Playlist> playlists) throws IOException {
        if (playlists == null) {
            logger.warn("playlist settings IS NULL");
            return;
        }
        Files.createDirectories(playListPath.getParent());
        objectMapper.writeValue(playListPath.toFile(), playlists);
    }

    @Override
    public Playlist getDefaultPlayList() {
        return new Playlist("Основной плейлист");
    }
}
