package ru.kantser.elephantmusic.controller;


import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kantser.elephantmusic.model.AppSettings;
import ru.kantser.elephantmusic.service.FxmlLoaderHelper;
import ru.kantser.elephantmusic.service.settings.JacksonSettingsService;
import ru.kantser.elephantmusic.view.update.UpdateWindow;

import java.io.IOException;

public class MainWindowController {
    private static final Logger logger = LoggerFactory.getLogger(MainWindowController.class);

    @Inject
    JacksonSettingsService settingsService;

    @Inject
    UpdateWindow updateWindow;

    private VBox playerPanel;
    private VBox playlistPanel;
    private VBox aboutPanel;
    private final VBox lastFmPanel;

    {
        try {
            lastFmPanel = FxmlLoaderHelper.load("/ru/kantser/elephantmusic/view/last_fm_panel.fxml");
        } catch (IOException e) {
            logger.warn("Исключение при создании панели LastFM");
            throw new RuntimeException(e);
        }
    }



    @FXML
    private StackPane contentArea;


    @FXML
    private Button currentButton;
    @FXML
    private Button playlistButton;
    @FXML
    private Button aboutButton;
    @FXML
    private Button lastFmButton;
    @FXML
    private Button updateButton;
    @FXML
    public void initialize() {
        if(settingsService == null){
            logger.warn("settingsService == null");
        }else {
            logger.info("Инициализирую настройки");
            AppSettings settings = null;
            settings = settingsService.loadSettings();


            settings.setLanguage("ENG");
            settingsService.saveSettings(settings);



        }
        try {
            // Загрузка панелей из отдельных FXML файлов
            logger.info("Сосздаю панель playerPanel");
            playerPanel = FxmlLoaderHelper.load("/ru/kantser/elephantmusic/view/player_panel.fxml");

            logger.info("Сосздаю панель playlistPanel");
            playlistPanel = FxmlLoaderHelper.load("/ru/kantser/elephantmusic/view/playlist_panel.fxml");
            if(playlistPanel == null){
                logger.warn("Созданный объект NULL");
            }

            logger.info("Сосздаю панель lastFm выполнено при инициализации класса");

            if(lastFmPanel == null){
                logger.warn("Созданный объект NULL");
            }else{
                logger.warn("Создан объект");
            }

            logger.info("Сосздаю панель aboutPanel");
            aboutPanel = FxmlLoaderHelper.load("/ru/kantser/elephantmusic/view/about_panel.fxml");

            showPlayerPanel();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    @FXML
    private void showPlayerPanel() {
        contentArea.getChildren().setAll(playerPanel);
        setActiveButton(currentButton);
    }

    @FXML
    private void showPlaylistPanel() {
        contentArea.getChildren().setAll(playlistPanel);
        setActiveButton(playlistButton);
    }

    @FXML
    private void showAboutPanel() {
        contentArea.getChildren().setAll(aboutPanel);
        setActiveButton(aboutButton);
    }

    public void showLastFmPanel() {
        if(lastFmPanel == null){
            logger.warn("lastFmPanel null объект");
        }
        try{
            contentArea.getChildren().setAll(lastFmPanel);
            setActiveButton(lastFmButton);
        }catch (Exception ex){
            logger.warn("Исключение во время создание панели LastFM {}", ex.getMessage());
            logger.info(ex.toString());
            ex.printStackTrace();
        }

    }

    @FXML
    private void showUpdateWindow() {
        updateWindow.show();
        updateWindow.toFront();
    }

    private void setActiveButton(Button activeButton) {
        currentButton.getStyleClass().remove("active");
        playlistButton.getStyleClass().remove("active");
        aboutButton.getStyleClass().remove("active");
        lastFmButton.getStyleClass().remove("active");
        updateButton.getStyleClass().remove("active");
        activeButton.getStyleClass().add("active");
    }


}