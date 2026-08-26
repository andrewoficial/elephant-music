package ru.kantser.elephantmusic.controller;

import com.google.inject.Inject;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kantser.elephantmusic.model.Track;
import ru.kantser.elephantmusic.service.AudioPlayerService;
import ru.kantser.elephantmusic.service.PlaylistService;

public class PlayerPanelController {
    private static final Logger logger = LoggerFactory.getLogger(PlayerPanelController.class);

    @FXML
    private Label currentTrackLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Button playPauseButton;

    @FXML
    private Button previousButton;

    @FXML
    private Button nextButton;

    @FXML
    private Slider volumeSlider;

    @FXML
    private Slider seekSlider;

    @Inject
    private AudioPlayerService audioPlayerService;

    @Inject
    private PlaylistService playlistService;


    private Timeline progressTimeline;

    @FXML
    public void initialize() {
        volumeSlider.setVisible(false);
        // Начальное состояние - воспроизведение
        playPauseButton.getStyleClass().add("ready-for-play");
        // Настройка слайдера громкости
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (audioPlayerService != null) {
                audioPlayerService.setVolume(newValue.doubleValue() / 100.0);
            }
        });

        // Настройка слайдера перемотки
        seekSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (seekSlider.isValueChanging() && audioPlayerService != null) {
                //logger.info("Set position {}", newValue.doubleValue());
                audioPlayerService.seek(newValue.doubleValue()); // Ожидает значение от 0 до 100
            }
        });

        // Создаем таймер для обновления прогресса воспроизведения
        progressTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.5), event -> updateProgress())
        );

        if (audioPlayerService != null) {
            audioPlayerService.addPlaybackStateListener(this::updatePlaybackStateFromService);
        }
        progressTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void updatePlaybackStateFromService() {
        if (audioPlayerService != null) {
            //updatePlaybackState(audioPlayerService.getCurrentTrack() != null);
            updatePlaybackState(audioPlayerService.isPlaying());
        }
    }

    private void updateProgress() {
        if (audioPlayerService != null && audioPlayerService.isPlaying()) {
            double progress = audioPlayerService.getCurrentPosition(); // Должен возвращать от 0 до 100
            double duration = audioPlayerService.getDuration(); // Должен возвращать общую длительность в секундах

            // Устанавливаем максимальное значение слайдера в 100
            seekSlider.setMax(100);
            seekSlider.setValue(progress);

            // Обновляем метку времени
            String currentTime = formatTime(progress * duration / 100); // progress в процентах
            String totalTime = formatTime(duration);
            timeLabel.setText(currentTime + " / " + totalTime);
        }
    }

    private String formatTime(double seconds) {
        int minutes = (int) seconds / 60;
        int secs = (int) seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    @FXML
    private void togglePlayPause() {
        if (audioPlayerService != null) {
            if (!audioPlayerService.isPlaying()) {
                logger.info("togglePlayPause переключаю на воспроизведение");
                audioPlayerService.resume();
                setPlayState(audioPlayerService.isPlaying());
            } else {
                logger.info("togglePlayPause переключаю на паузу");
                audioPlayerService.pause();
                setPlayState(false);
            }
        }
    }

    @FXML
    private void previousTrack() {
        if (audioPlayerService != null && playlistService != null) {
            Track previousTrack = playlistService.getPreviousTrack();
            if (previousTrack != null) {
                audioPlayerService.play(previousTrack);
                setPlayState(true);
            }
        }
    }

    @FXML
    private void nextTrack() {
        if (audioPlayerService != null && playlistService != null) {
            Track nextTrack = playlistService.getNextTrack();
            if (nextTrack != null) {
                audioPlayerService.play(nextTrack);
                setPlayState(true);
            }
        }
    }

    private void updateTrackInfo() {
        if (audioPlayerService.getCurrentTrack() != null) {
            currentTrackLabel.setText(
                    audioPlayerService.getCurrentTrack().getArtist() + " - " +
                            audioPlayerService.getCurrentTrack().getTitle()
            );
        }
    }

    // Метод для обновления UI при изменении состояния воспроизведения извне
    public void updatePlaybackState(boolean isPlaying) {
        logger.info("Уведомление о начале проигрывания{}", isPlaying);
        setPlayState(isPlaying);

    }

    private void setPlayState(boolean state){
        if(state){
            logger.info("Ставлю ПЛЕЙ");
            playPauseButton.getStyleClass().remove("ready-for-play");
            progressTimeline.play();
        }else{
            logger.info("Ставлю СТОП");
            playPauseButton.getStyleClass().add("ready-for-play");
            progressTimeline.stop();
        }
        updateTrackInfo();
    }
}