package ru.kantser.elephantmusic.view.dialog;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kantser.elephantmusic.model.IdentificationResult;
import ru.kantser.elephantmusic.model.TagFields;
import ru.kantser.elephantmusic.model.Track;
import ru.kantser.elephantmusic.service.identification.TrackIdentificationProvider;
import ru.kantser.elephantmusic.service.identification.TrackIdentificationRegistry;
import ru.kantser.elephantmusic.service.tag.TagService;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class TagEditorDialog extends Stage {
    private static final Logger logger = LoggerFactory.getLogger(TagEditorDialog.class);

    private final TagService tagService;
    private final TrackIdentificationRegistry identificationRegistry;

    @Inject
    public TagEditorDialog(TagService tagService, TrackIdentificationRegistry identificationRegistry) {
        this.tagService = tagService;
        this.identificationRegistry = identificationRegistry;
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Редактирование тегов");
        setResizable(false);
    }

    public boolean open(Track track) {
        Path path = track.getFilePath();
        TagFields v1 = tagService.readV1(path);
        TagFields v2 = tagService.readV2(path);

        boolean mp3 = tagService.isMp3(path);

        Form v1Form = new Form(v1);
        Form v2Form = new Form(v2);

        VBox header = new VBox(6, buildCoverArt(path), buildTrackInfo(track));

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        if (mp3) {
            tabPane.getTabs().add(buildV1Tab(v1Form));
            tabPane.getTabs().add(buildV2Tab(v2Form));
        } else {
            VBox notice = new VBox(new Label("Редактирование ID3 тегов доступно только для MP3 файлов."));
            notice.setPadding(new Insets(20));
            Tab tab = new Tab("Теги", notice);
            tabPane.getTabs().add(tab);
        }

        Button saveButton = new Button("Сохранить");
        Button cancelButton = new Button("Отмена");
        saveButton.setDefaultButton(true);
        cancelButton.setCancelButton(true);

        HBox buttons = new HBox(8, saveButton, cancelButton);
        buttons.setPadding(new Insets(12, 0, 0, 0));
        buttons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        VBox providerBox = new VBox(6);
        providerBox.setPadding(new Insets(12, 0, 0, 0));
        providerBox.getChildren().add(new Label("Определить трек:"));
        HBox providerButtons = new HBox(8);
        List<Button> providerButtonList = new ArrayList<>();
        for (TrackIdentificationProvider provider : identificationRegistry.getProviders()) {
            Button button = new Button(provider.getName());
            button.setOnAction(e -> runIdentification(provider, providerButtonList, path, v1Form, v2Form));
            providerButtons.getChildren().add(button);
            providerButtonList.add(button);
        }
        providerBox.getChildren().add(providerButtons);

        VBox root = new VBox(10, header, tabPane, buttons, providerBox);
        root.setPadding(new Insets(16));
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        final boolean[] saved = {false};

        saveButton.setOnAction(e -> {
            if (!mp3) {
                close();
                return;
            }
            try {
                tagService.write(path, v1Form.toFields(), v2Form.toFields());
                applyToTrack(track, v2Form.toFields(), v1Form.toFields());
                saved[0] = true;
                close();
            } catch (Exception ex) {
                logger.error("Не удалось сохранить теги", ex);
                Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось сохранить теги:\n" + ex.getMessage(), ButtonType.OK);
                alert.setHeaderText(null);
                alert.showAndWait();
            }
        });

        cancelButton.setOnAction(e -> close());

        setScene(new Scene(root));
        sizeToScene();
        showAndWait();
        return saved[0];
    }

    private void runIdentification(TrackIdentificationProvider provider, List<Button> allButtons,
                                   Path path, Form v1Form, Form v2Form) {
        allButtons.forEach(b -> b.setDisable(true));
        provider.identify(path).thenAccept(result -> Platform.runLater(() -> {
            allButtons.forEach(b -> b.setDisable(false));
            handleIdentificationResult(provider, result, v1Form, v2Form);
        }));
    }

    private void handleIdentificationResult(TrackIdentificationProvider provider, IdentificationResult result,
                                            Form v1Form, Form v2Form) {
        if (!result.isSuccess()) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "Не удалось определить трек.\n" + result.getErrorMessage());
            alert.setTitle("Распознавание (" + provider.getName() + ")");
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }

        StringBuilder message = new StringBuilder(result.getArtist() + " - " + result.getTitle());
        if (result.getAlbum() != null && !result.getAlbum().isBlank()) {
            message.append("\nАльбом: ").append(result.getAlbum());
        }
        message.append("\n\nЗаполнить поля тегов полученными данными?");

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Распознавание (" + provider.getName() + ")");
        confirm.setHeaderText("Распознанный трек:");
        confirm.setContentText(message.toString());
        confirm.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                v1Form.fill(result.getTitle(), result.getArtist(), result.getAlbum());
                v2Form.fill(result.getTitle(), result.getArtist(), result.getAlbum());
            }
        });
    }

    private ImageView buildCoverArt(Path path) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        byte[] coverData = tagService.readCoverArt(path);
        if (coverData != null) {
            try {
                imageView.setImage(new Image(new ByteArrayInputStream(coverData)));
            } catch (Exception e) {
                logger.warn("Не удалось загрузить обложку: {}", e.getMessage());
            }
        }
        return imageView;
    }

    private VBox buildTrackInfo(Track track) {
        Label title = new Label(track.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label artist = new Label(track.getArtist());
        return new VBox(2, title, artist);
    }

    private Tab buildV1Tab(Form form) {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(16));

        addRow(grid, 0, "Название", form.title);
        addRow(grid, 1, "Исполнитель", form.artist);
        addRow(grid, 2, "Альбом", form.album);
        addRow(grid, 3, "Год", form.year);
        addRow(grid, 4, "Комментарий", form.comment);
        addRow(grid, 5, "Жанр", form.genre);

        Tab tab = new Tab("ID3v1", grid);
        tab.setClosable(false);
        return tab;
    }

    private Tab buildV2Tab(Form form) {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(16));

        addRow(grid, 0, "Название", form.title);
        addRow(grid, 1, "Исполнитель", form.artist);
        addRow(grid, 2, "Альбом", form.album);
        addRow(grid, 3, "Год", form.year);
        addRow(grid, 4, "Номер трека", form.track);
        addRow(grid, 5, "Комментарий", form.comment);
        addRow(grid, 6, "Жанр", form.genre);
        addRow(grid, 7, "Композитор", form.composer);

        Tab tab = new Tab("ID3v2", grid);
        tab.setClosable(false);
        return tab;
    }

    private void addRow(GridPane grid, int row, String label, TextField field) {
        grid.add(new Label(label), 0, row);
        grid.add(field, 1, row);
        field.setPrefColumnCount(30);
    }

    private void applyToTrack(Track track, TagFields v2, TagFields v1) {
        String title = firstNonBlank(v2.getTitle(), v1.getTitle());
        String artist = firstNonBlank(v2.getArtist(), v1.getArtist());
        if (title != null) {
            track.setTitle(title);
        }
        if (artist != null) {
            track.setArtist(artist);
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static class Form {
        final TextField title;
        final TextField artist;
        final TextField album;
        final TextField year;
        final TextField track;
        final TextField comment;
        final TextField genre;
        final TextField composer;

        Form(TagFields fields) {
            this.title = new TextField(nvl(fields.getTitle()));
            this.artist = new TextField(nvl(fields.getArtist()));
            this.album = new TextField(nvl(fields.getAlbum()));
            this.year = new TextField(nvl(fields.getYear()));
            this.track = new TextField(nvl(fields.getTrack()));
            this.comment = new TextField(nvl(fields.getComment()));
            this.genre = new TextField(nvl(fields.getGenre()));
            this.composer = new TextField(nvl(fields.getComposer()));
        }

        void fill(String newTitle, String newArtist, String newAlbum) {
            if (newTitle != null && !newTitle.isBlank()) {
                title.setText(newTitle);
            }
            if (newArtist != null && !newArtist.isBlank()) {
                artist.setText(newArtist);
            }
            if (newAlbum != null && !newAlbum.isBlank()) {
                album.setText(newAlbum);
            }
        }

        TagFields toFields() {
            TagFields fields = new TagFields();
            fields.setTitle(title.getText());
            fields.setArtist(artist.getText());
            fields.setAlbum(album.getText());
            fields.setYear(year.getText());
            fields.setTrack(track.getText());
            fields.setComment(comment.getText());
            fields.setGenre(genre.getText());
            fields.setComposer(composer.getText());
            return fields;
        }

        private static String nvl(String value) {
            return value == null ? "" : value;
        }
    }
}
