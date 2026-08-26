package ru.kantser.elephantmusic.controller;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kantser.elephantmusic.model.AppSettings;
import ru.kantser.elephantmusic.service.settings.JacksonSettingsService;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SettingsPanelController {
    private static final Logger logger = LoggerFactory.getLogger(SettingsPanelController.class);

    @Inject
    private JacksonSettingsService settingsService;

    @FXML
    private TextField auddTokenField;
    @FXML
    private TextField acrAccessKeyField;
    @FXML
    private TextField acrAccessSecretField;
    @FXML
    private TextField acrHostField;
    @FXML
    private Label settingsFolderLabel;

    @FXML
    public void initialize() {
        settingsFolderLabel.setText("Папка: " + settingsFolder().toAbsolutePath());
        load();
    }

    @FXML
    private void openSettingsFolder() {
        Path dir = settingsFolder();
        try {
            Files.createDirectories(dir);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(dir.toFile());
            } else {
                logger.warn("Desktop не поддерживается, не удалось открыть папку");
            }
        } catch (IOException e) {
            logger.warn("Не удалось открыть папку настроек: {}", e.getMessage());
        }
    }

    @FXML
    private void saveSettings() {
        AppSettings settings = settingsService.loadSettings();
        settings.setAuddToken(trimToNull(auddTokenField.getText()));
        settings.setAcrAccessKey(trimToNull(acrAccessKeyField.getText()));
        settings.setAcrAccessSecret(trimToNull(acrAccessSecretField.getText()));
        settings.setAcrHost(trimToNull(acrHostField.getText()));
        settingsService.saveSettings(settings);
        logger.info("Настройки ключей сохранены");
    }

    private void load() {
        AppSettings settings = settingsService.loadSettings();
        auddTokenField.setText(nvl(settings.getAuddToken()));
        acrAccessKeyField.setText(nvl(settings.getAcrAccessKey()));
        acrAccessSecretField.setText(nvl(settings.getAcrAccessSecret()));
        acrHostField.setText(nvl(settings.getAcrHost()));
    }

    private Path settingsFolder() {
        return Paths.get(System.getProperty("user.home"), ".ElephantPlayer");
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
