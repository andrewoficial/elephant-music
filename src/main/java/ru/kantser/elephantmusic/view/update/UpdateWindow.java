package ru.kantser.elephantmusic.view.update;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kantser.elephantmusic.model.AppSettings;
import ru.kantser.elephantmusic.model.UpdateInfo;
import ru.kantser.elephantmusic.model.UpdateSource;
import ru.kantser.elephantmusic.service.settings.SettingsService;
import ru.kantser.elephantmusic.service.update.ApplicationInfoService;
import ru.kantser.elephantmusic.service.update.UpdateService;

import java.io.IOException;
import java.util.List;

@Singleton
public class UpdateWindow extends Stage {
    private static final Logger log = LoggerFactory.getLogger(UpdateWindow.class);

    private final UpdateService updateService;
    private final ApplicationInfoService appInfo;
    private final SettingsService settingsService;
    private final String currentVersion;
    private List<UpdateSource> sources;

    private ComboBox<String> sourceCombo;
    private Button checkButton;
    private TextArea checkResultArea;
    private WebView whatsNewView;
    private Button downloadButton;
    private ProgressBar progressBar;
    private Label progressLabel;
    private TextField statusField;

    private UpdateInfo pendingUpdate;
    private boolean updating;

    @Inject
    public UpdateWindow(UpdateService updateService, ApplicationInfoService appInfo,
                        SettingsService settingsService) {
        this.updateService = updateService;
        this.appInfo = appInfo;
        this.settingsService = settingsService;
        this.currentVersion = appInfo.getVersion();
        this.sources = updateService.getSources();

        initModality(Modality.NONE);
        setTitle("Проверка обновлений");
        setResizable(true);

        createUI();
        refreshSourceCombo();

        setMinWidth(520);
        setMinHeight(500);
        setWidth(540);
        setHeight(560);
    }

    private void createUI() {
        sourceCombo = new ComboBox<>();
        sourceCombo.setPrefWidth(200);

        checkButton = new Button("Проверить");
        checkButton.setPrefSize(140, 32);

        Button settingsButton = new Button("Настройки");
        settingsButton.setPrefSize(100, 32);
        settingsButton.setOnAction(e -> showSettingsDialog());

        HBox sourceRow = new HBox(8, new Label("Источник:"), sourceCombo, checkButton, settingsButton);
        sourceRow.setPadding(new Insets(12, 12, 0, 12));
        sourceRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        checkResultArea = new TextArea();
        checkResultArea.setEditable(false);
        checkResultArea.setWrapText(true);
        checkResultArea.setPrefHeight(80);
        checkResultArea.setStyle("-fx-font-family: sans-serif; -fx-font-size: 12px;");

        whatsNewView = new WebView();
        whatsNewView.setPrefHeight(200);

        VBox.setVgrow(whatsNewView, Priority.ALWAYS);

        downloadButton = new Button("Скачать");
        downloadButton.setPrefSize(140, 32);
        downloadButton.setDisable(true);

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(24);
        progressBar.setStyle("-fx-accent: #4CAF50;");

        progressLabel = new Label("0%");
        progressLabel.setVisible(false);

        HBox downloadRow = new HBox(8, downloadButton, progressBar, progressLabel);
        downloadRow.setPadding(new Insets(4, 12, 0, 12));
        HBox.setHgrow(progressBar, Priority.ALWAYS);

        statusField = new TextField();
        statusField.setEditable(false);
        statusField.setPrefHeight(30);

        VBox root = new VBox(8,
                sourceRow,
                checkResultArea,
                whatsNewView,
                downloadRow,
                statusField
        );
        root.setPadding(new Insets(0, 12, 12, 12));
        VBox.setVgrow(whatsNewView, Priority.ALWAYS);

        Scene scene = new Scene(root);
        setScene(scene);

        checkButton.setOnAction(e -> checkForUpdates());
        downloadButton.setOnAction(e -> downloadUpdate());
    }

    private void refreshSourceCombo() {
        sources = updateService.getSources();
        sourceCombo.getItems().clear();
        for (UpdateSource src : sources)
            sourceCombo.getItems().add(src.getName());
        if (!sources.isEmpty())
            sourceCombo.getSelectionModel().select(0);
    }

    private void showSettingsDialog() {
        AppSettings settings = null;
        try {
            settings = settingsService.loadSettings();
        } catch (IOException e) {
            log.warn(" {}", e.getMessage());
            return;
            //throw new RuntimeException(e);
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Настройки обновлений");
        dialog.setHeaderText("Укажите свой источник обновлений и токен");

        TextField sourceField = new TextField(settings.getUpdateSourceUrl());
        sourceField.setPromptText("http://example.com/user/repo");
        sourceField.setPrefWidth(350);

        TextField tokenField = new TextField(settings.getUpdateToken());
        tokenField.setPromptText("Токен для Gitea (опционально)");

        VBox content = new VBox(10,
                new Label("Пользовательский источник:"),
                sourceField,
                new Label("Токен:"),
                tokenField
        );
        content.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        AppSettings finalSettings = settings;
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                finalSettings.setUpdateSourceUrl(sourceField.getText().trim());
                finalSettings.setUpdateToken(tokenField.getText().trim());
                try {
                    settingsService.saveSettings(finalSettings);
                } catch (IOException e) {
                    log.warn("Failed to save settings", e);
                    //throw new RuntimeException(e);
                }
                refreshSourceCombo();
            }
        });
    }

    private UpdateSource getSelectedSource() {
        int idx = sourceCombo.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < sources.size())
            return sources.get(idx);
        return null;
    }

    private void checkForUpdates() {
        if (updating) return;
        updating = true;

        checkButton.setDisable(true);
        downloadButton.setDisable(true);
        pendingUpdate = null;
        checkResultArea.setText("Проверка обновлений...");
        whatsNewView.getEngine().loadContent("");
        statusField.setText("Проверяю...");
        progressLabel.setVisible(false);

        UpdateSource selectedSource = getSelectedSource();
        if (selectedSource == null) {
            checkResultArea.setText("Не выбран источник обновлений.");
            statusField.setText("Ошибка");
            checkButton.setDisable(false);
            updating = false;
            return;
        }

        updateService.checkForUpdates(selectedSource).thenAccept(updateInfo -> {
            Platform.runLater(() -> {
                if (updateInfo != null) {
                    pendingUpdate = updateInfo;
                    checkResultArea.setText("Доступна новая версия: " + updateInfo.getVersion());
                    whatsNewView.getEngine().loadContent(toHtml(updateInfo.getReleaseNotes()));
                    downloadButton.setDisable(false);
                    statusField.setText("Обновление найдено");
                } else {
                    checkResultArea.setText("Нет доступных обновлений.");
                    whatsNewView.getEngine().loadContent("<html><body>Текущая версия: " + currentVersion + "</body></html>");
                    statusField.setText("Обновлений не найдено");
                }
                checkButton.setDisable(false);
                updating = false;
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                checkResultArea.setText("Ошибка проверки обновлений:\n" + ex.getCause().getMessage());
                statusField.setText("Ошибка");
                checkButton.setDisable(false);
                updating = false;
            });
            return null;
        });
    }

    private void downloadUpdate() {
        if (pendingUpdate == null) return;

        downloadButton.setDisable(true);
        checkButton.setDisable(true);
        progressBar.setProgress(0);
        progressBar.setVisible(true);
        progressLabel.setVisible(true);
        progressLabel.setText("0%");
        statusField.setText("Скачивание...");

        updateService.downloadAndPrepareUpdate(pendingUpdate, percent ->
                Platform.runLater(() -> {
                    if (percent >= 0 && percent <= 100) {
                        progressBar.setProgress(percent / 100.0);
                        progressLabel.setText(percent + "%");
                    }
                })
        ).thenAccept(newJar -> {
            Platform.runLater(() -> {
                progressBar.setProgress(1.0);
                progressLabel.setText("100%");
                statusField.setText("Загрузка завершена!");

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "Обновление загружено. Установить сейчас?",
                        ButtonType.YES, ButtonType.NO);
                alert.setTitle("Установка обновления");
                alert.setHeaderText(null);
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        try {
                            updateService.applyUpdate(newJar);
                        } catch (Exception ex) {
                            Alert errAlert = new Alert(Alert.AlertType.ERROR,
                                    "Ошибка при установке обновления:\n" + ex.getMessage());
                            errAlert.setTitle("Ошибка");
                            errAlert.setHeaderText(null);
                            errAlert.showAndWait();
                        }
                    }
                });
                checkButton.setDisable(false);
                downloadButton.setDisable(false);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                progressBar.setProgress(0);
                progressLabel.setVisible(false);
                statusField.setText("Ошибка загрузки: " + ex.getCause().getMessage());
                downloadButton.setDisable(false);
                checkButton.setDisable(false);
            });
            return null;
        });
    }

    private String toHtml(String markdown) {
        if (markdown == null || markdown.isEmpty())
            return "<html><body></body></html>";

        String text = markdown
                .replace("\\r\\n", "\n")
                .replace("\\n", "\n")
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\r\n", "\n")
                .replace("\r", "\n");

        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family:sans-serif;font-size:12px;padding:4px'>\n");

        String[] lines = text.split("\n", -1);
        boolean inUl = false;
        boolean inOl = false;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.startsWith("## ")) {
                if (inUl) { html.append("</ul>\n"); inUl = false; }
                if (inOl) { html.append("</ol>\n"); inOl = false; }
                html.append("<h2 style='color:#1a73e8;margin:8px 0 4px 0'>")
                    .append(trimmed.substring(3).trim())
                    .append("</h2>\n");
            } else if (trimmed.startsWith("- ")) {
                if (inOl) { html.append("</ol>\n"); inOl = false; }
                if (!inUl) { html.append("<ul style='margin:4px 0'>\n"); inUl = true; }
                html.append("<li>").append(trimmed.substring(2).trim()).append("</li>\n");
            } else if (trimmed.matches("^\\d+\\.\\s.*")) {
                if (inUl) { html.append("</ul>\n"); inUl = false; }
                if (!inOl) { html.append("<ol style='margin:4px 0'>\n"); inOl = true; }
                html.append("<li>").append(trimmed.replaceFirst("^\\d+\\.\\s+", "")).append("</li>\n");
            } else if (trimmed.isEmpty()) {
                if (inUl) { html.append("</ul>\n"); inUl = false; }
                if (inOl) { html.append("</ol>\n"); inOl = false; }
                html.append("<br>\n");
            } else {
                if (inUl) { html.append("</ul>\n"); inUl = false; }
                if (inOl) { html.append("</ol>\n"); inOl = false; }
                html.append("<p style='margin:2px 0'>").append(trimmed).append("</p>\n");
            }
        }
        if (inUl) html.append("</ul>\n");
        if (inOl) html.append("</ol>\n");

        html.append("</body></html>");
        return html.toString();
    }
}
