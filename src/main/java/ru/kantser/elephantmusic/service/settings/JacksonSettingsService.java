package ru.kantser.elephantmusic.service.settings;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kantser.elephantmusic.model.AppSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Singleton
public class JacksonSettingsService implements SettingsService {
    private static final Logger logger = LoggerFactory.getLogger(JacksonSettingsService.class);

    private final ObjectMapper objectMapper;
    private final Path settingsPath;

    @Inject
    public JacksonSettingsService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.settingsPath = Paths.get(System.getProperty("user.home"), ".ElephantPlayer", "settings.json");
    }

    @Override
    public AppSettings loadSettings(){
        if (Files.exists(settingsPath)) {
            try {
                return objectMapper.readValue(settingsPath.toFile(), AppSettings.class);
            } catch (IOException e) {
                return getDefaultSettings();
                //throw new RuntimeException(e);
            }
        } else {
            return getDefaultSettings();
        }
    }



    @Override
    public void saveSettings(AppSettings settings) {
        logger.info("Сохраняю в {}", settingsPath.getParent());
        if(settings == null){
            logger.warn("AppSettings settings IS NULL");
        }
        try {
            Files.createDirectories(settingsPath.getParent());
        } catch (IOException e) {
            logger.warn("AppSettings settings saving error {}", e.getMessage());
            //throw new RuntimeException(e);
        }
        //logger.info("Создал файл {}", settingsPath.toFile());
        logger.info("Язык в новых настройках {}", settings.getLanguage());
        try {
            objectMapper.writeValue(settingsPath.toFile(), settings);
        } catch (IOException e) {
            logger.warn("AppSettings settings writing file error {}", e.getMessage());
            //throw new RuntimeException(e);
        }
    }

    @Override
    public AppSettings getDefaultSettings() {
        AppSettings defaults = new AppSettings();
        defaults.setLanguage("RU");
        defaults.setLastFmName("Anonim");
        defaults.setLastFmToken("NULL");
        defaults.setActiveScrobbling(true);
        defaults.setUpdateSourceUrl("");
        defaults.setUpdateToken("");
        return defaults;
    }
}