package ru.kantser.elephantmusic.controller;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kantser.elephantmusic.model.Playlist;
import ru.kantser.elephantmusic.model.Track;
import ru.kantser.elephantmusic.service.AudioPlayerService;
import ru.kantser.elephantmusic.service.PlaylistService;
import ru.kantser.elephantmusic.service.WindowTitleService;
import ru.kantser.elephantmusic.view.dialog.TagEditorDialog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayListPanelController {
    private static final Logger logger = LoggerFactory.getLogger(PlayListPanelController.class);

    @FXML
    private TabPane playlistTabs;

    @Inject
    private PlaylistService playlistService;

    @Inject
    private AudioPlayerService audioPlayerService;

    @Inject
    private WindowTitleService windowTitleService;

    @Inject
    private TagEditorDialog tagEditorDialog;

    @FXML
    public void initialize() {
        playlistService.getPlaylists().forEach(this::addPlaylistTab);

        playlistTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && newTab.getUserData() instanceof Playlist playlist) {
                playlistService.setCurrentPlaylist(playlist);
            }
        });
    }

    private void addPlaylistTab(Playlist playlist) {
        ListView<Track> view = new ListView<>();
        view.setItems(playlist.getTracks());
        view.setCellFactory(lv -> new ListCell<Track>() {
            @Override
            protected void updateItem(Track item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.toString());
            }
        });
        view.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Track selectedTrack = view.getSelectionModel().getSelectedItem();
                if (selectedTrack != null) {
                    playTrack(selectedTrack);
                }
            }
        });
        view.setContextMenu(createTrackContextMenu(view, playlist));

        Tab tab = new Tab(playlist.getName(), view);
        tab.setUserData(playlist);
        tab.setContextMenu(createTabContextMenu(playlist, tab));

        playlistTabs.getTabs().add(tab);
    }

    private Tab findTab(Playlist playlist) {
        for (Tab tab : playlistTabs.getTabs()) {
            if (tab.getUserData() == playlist) {
                return tab;
            }
        }
        return null;
    }

    private ContextMenu createTrackContextMenu(ListView<Track> view, Playlist playlist) {
        ContextMenu menu = new ContextMenu();

        MenuItem playItem = new MenuItem("Воспроизвести");
        MenuItem removeItem = new MenuItem("Удалить из плейлиста");
        MenuItem removeItemFromPc = new MenuItem("Удалить файл с компьютера");
        MenuItem moveItem = new MenuItem("Перенести в другой плей-лист");
        MenuItem copyItem = new MenuItem("Копировать в другой плей-лист");
        MenuItem editTagsItem = new MenuItem("Редактировать теги");

        playItem.setOnAction(e -> {
            Track selected = view.getSelectionModel().getSelectedItem();
            if (selected != null) playTrack(selected);
        });

        removeItem.setOnAction(e -> {
            Track selected = view.getSelectionModel().getSelectedItem();
            if (selected != null) playlistService.removeTrack(selected);
        });

        removeItemFromPc.setOnAction(e -> {
            Track selected = view.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Удаление файла");
            confirm.setHeaderText("Удалить файл с компьютера?");
            confirm.setContentText("Трек «" + selected + "» будет удалён не только из плейлиста, но и с вашего компьютера. Это действие нельзя отменить.");
            confirm.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    Path path = selected.getFilePath();
                    logger.info("К удалению файл: {}", path.getFileName());
                    logger.info("Размер файла: {} байт", safeFileSize(path));
                    logger.info("Полный путь: {}", path.toAbsolutePath());
                    logger.info("Права на чтение: {}, на запись: {}", Files.isReadable(path), Files.isWritable(path));
                    logger.info("Занят программой: {}", playlistService.isFileLocked(path) ? "да" : "нет");
                    logger.info("Сейчас воспроизводится: {}", audioPlayerService.isCurrentTrack(selected) ? "да" : "нет");

                    boolean wasPlaying = audioPlayerService.isCurrentTrack(selected);
                    Track replacement = wasPlaying ? playlistService.getReplacementTrack(selected) : null;

                    audioPlayerService.stopAndDisposeIfCurrent(selected);
                    playlistService.removeTrack(selected);

                    logger.info("После освобождения плеером занят: {}", playlistService.isFileLocked(path) ? "да" : "нет");

                    if (wasPlaying && replacement != null) {
                        playTrack(replacement);
                    }

                    new Thread(() -> {
                        boolean deleted = playlistService.deleteFileCompletely(selected);
                        if (!deleted) {
                            Platform.runLater(() -> {
                                Alert info = new Alert(Alert.AlertType.INFORMATION);
                                info.setTitle("Удаление файла");
                                info.setHeaderText(null);
                                info.setContentText("Трек удалён из плейлиста, но файл на компьютере не был найден и не удалён.");
                                info.showAndWait();
                            });
                        }
                    }).start();
                }
            });
        });

        moveItem.setOnAction(e -> {
            Track selected = view.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            Playlist target = chooseTargetPlaylist(playlist);
            if (target != null) playlistService.moveTrack(selected, target);
        });

        copyItem.setOnAction(e -> {
            Track selected = view.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            Playlist target = chooseTargetPlaylist(playlist);
            if (target != null) playlistService.copyTrack(selected, target);
        });

        editTagsItem.setOnAction(e -> {
            Track selected = view.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            if (tagEditorDialog.open(selected)) {
                view.refresh();
            }
        });

        menu.getItems().addAll(playItem, removeItem, removeItemFromPc,
                new SeparatorMenuItem(), moveItem, copyItem,
                new SeparatorMenuItem(), editTagsItem);
        return menu;
    }

    private ContextMenu createTabContextMenu(Playlist playlist, Tab tab) {
        ContextMenu menu = new ContextMenu();

        MenuItem renameItem = new MenuItem("Переименовать");
        MenuItem deleteItem = new MenuItem("Удалить лист");
        MenuItem exportItem = new MenuItem("Экспортировать лист");
        MenuItem copyAllItem = new MenuItem("Копировать все файлы");

        renameItem.setOnAction(e -> logger.info("Переименование плейлиста пока не реализовано"));
        deleteItem.setOnAction(e -> logger.info("Удаление плейлиста пока не реализовано"));
        exportItem.setOnAction(e -> logger.info("Экспорт плейлиста пока не реализован"));
        copyAllItem.setOnAction(e -> logger.info("Копирование файлов пока не реализовано"));

        menu.getItems().addAll(renameItem, deleteItem, exportItem, copyAllItem);
        return menu;
    }

    private Playlist chooseTargetPlaylist(Playlist source) {
        Dialog<Playlist> dialog = new Dialog<>();
        dialog.setTitle("Выбор плейлиста");
        dialog.setHeaderText("Перенос или копирование в другой плей-лист");

        ButtonType selectType = new ButtonType("Выбрать", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectType, ButtonType.CANCEL);

        ListView<Playlist> listView = new ListView<>();
        listView.getItems().addAll(playlistService.getPlaylists());
        listView.getItems().remove(source);
        listView.setPrefHeight(120);

        TextField newNameField = new TextField();
        newNameField.setPromptText("Или введите название нового плейлиста");

        VBox content = new VBox(8, new Label("Существующие плейлисты:"), listView, newNameField);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(button -> {
            if (button == selectType) {
                String newName = newNameField.getText();
                if (newName != null && !newName.trim().isEmpty()) {
                    return playlistService.createPlaylist(newName.trim());
                }
                return listView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        Optional<Playlist> result = dialog.showAndWait();
        Playlist target = result.orElse(null);
        if (target != null && findTab(target) == null) {
            addPlaylistTab(target);
            playlistTabs.getSelectionModel().select(findTab(target));
        }
        return target;
    }

    private void playTrack(Track track) {
        if (audioPlayerService != null) {
            audioPlayerService.play(track);
            playlistService.setCurrentTrack(track);
            if (windowTitleService != null) {
                windowTitleService.updateTitle("Воспроизведение: " + track.getTitle());
            }
            audioPlayerService.notifyPlaybackStateChanged(true);
        } else {
            logger.warn("AudioPlayerService не доступен для воспроизведения трека");
        }
    }

    @FXML
    private void addTrack() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите аудио файл");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Аудио файлы", "*.mp3", "*.wav", "*.aac", "*.flac", "*.m4a")
        );

        File file = fileChooser.showOpenDialog(playlistTabs.getScene().getWindow());
        if (file != null) {
            playlistService.addTrack(parseAudioFile(file));
        }
    }

    @FXML
    private void clearPlaylist() {
        playlistService.clearPlaylist();
    }

    @FXML
    private void addFolderForScan(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Выберите папку с аудиофайлами");

        File selectedDirectory = directoryChooser.showDialog(playlistTabs.getScene().getWindow());

        if (selectedDirectory != null) {
            Playlist newPlaylist = playlistService.createPlaylist(selectedDirectory.getName());
            addPlaylistTab(newPlaylist);
            playlistTabs.getSelectionModel().select(findTab(newPlaylist));
            playlistService.setCurrentPlaylist(newPlaylist);

            new Thread(() -> {
                List<Track> collected = new ArrayList<>();
                try {
                    Files.walk(selectedDirectory.toPath())
                            .filter(this::isAudioFile)
                            .forEach(filePath -> {
                                Track track = parseAudioFile(filePath.toFile());
                                if (track != null) {
                                    collected.add(track);
                                }
                            });
                } catch (IOException e) {
                    logger.error("Ошибка при сканировании папки: " + selectedDirectory, e);
                }

                Platform.runLater(() -> {
                    playlistService.addTracks(newPlaylist, collected);
                    showCompletionAlert(collected.size());
                });
            }).start();
        }
    }

    private boolean isAudioFile(Path path) {
        if (!Files.isRegularFile(path)) {
            return false;
        }
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".mp3") ||
                fileName.endsWith(".wav") ||
                fileName.endsWith(".aac") ||
                fileName.endsWith(".flac") ||
                fileName.endsWith(".m4a");
    }

    private long safeFileSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            return -1;
        }
    }

    private Track parseAudioFile(File file) {
        try {
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();
            AudioHeader header = audioFile.getAudioHeader();

            String title = file.getName();
            String artist = "Неизвестный исполнитель";
            Duration duration = Duration.ofSeconds(header.getTrackLength());

            if (tag != null) {
                if (tag.getFirst(FieldKey.TITLE) != null && !tag.getFirst(FieldKey.TITLE).isEmpty()) {
                    title = tag.getFirst(FieldKey.TITLE);
                }
                if (tag.getFirst(FieldKey.ARTIST) != null && !tag.getFirst(FieldKey.ARTIST).isEmpty()) {
                    artist = tag.getFirst(FieldKey.ARTIST);
                }
            }

            return new Track(title, artist, file.toPath(), duration);
        } catch (Exception e) {
            logger.warn("Ошибка чтения метаданных: {}", e.getMessage());
            return new Track(file.getName(), "Неизвестный исполнитель", file.toPath(), Duration.ofSeconds(0));
        }
    }

    private void showCompletionAlert(int tracksAdded) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Сканирование завершено");
        alert.setHeaderText(null);
        alert.setContentText("Добавлено треков: " + tracksAdded);
        alert.showAndWait();
    }
}
