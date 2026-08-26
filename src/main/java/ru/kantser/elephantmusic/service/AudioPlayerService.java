package ru.kantser.elephantmusic.service;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kantser.elephantmusic.model.Playlist;
import ru.kantser.elephantmusic.model.PlayerState;
import ru.kantser.elephantmusic.model.Track;
import ru.kantser.elephantmusic.service.lastfm.LastFmScrobblerService;
import ru.kantser.elephantmusic.service.settings.PlayerStateService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioPlayerService {
    private MediaPlayer mediaPlayer;
    private Track currentTrack;
    private ChangeListener<Duration> progressListener;
    private AtomicBoolean isPlaying = new AtomicBoolean(false);
    private AtomicBoolean hasScrobbled = new AtomicBoolean(false); // Флаг для скробблинга (чтобы не дублировать)
    private static final Logger logger = LoggerFactory.getLogger(AudioPlayerService.class);
    private Runnable onTrackChanged;
    private List<Runnable> playbackStateListeners = new ArrayList<>();

    @Inject
    private PlaylistService playlistService;

    @Inject
    private WindowTitleService windowTitleService;

    @Inject
    private LastFmScrobblerService scrobbler; // Инжект скробблера

    @Inject
    private PlayerStateService playerStateService;

    public void setOnTrackChanged(Runnable listener) {
        this.onTrackChanged = listener;
    }

    private void notifyTrackChanged() {
        if (onTrackChanged != null) {
            Platform.runLater(onTrackChanged);
        }
    }

    public void play(Track track) {
        disposePlayer();

        currentTrack = track;
        Media media = new Media(track.getFilePath().toUri().toString());
        mediaPlayer = new MediaPlayer(media);
        isPlaying.set(true);
        hasScrobbled.set(false); // Сбрасываем флаг скробблинга для нового трека

        savePlayerState(0);

        progressListener = new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                if (hasScrobbled.get()) return; // Уже скробблили — выходим

                MediaPlayer player = mediaPlayer;
                if (player == null) return;

                double playedSeconds = newValue.toSeconds();
                double totalSeconds = player.getTotalDuration().toSeconds();
                double playedPercent = (playedSeconds / totalSeconds) * 100;

                // Проверяем правило: >50% или >240 сек
                if (playedPercent >= 50 || playedSeconds >= 240) {
                    scrobbler.scrobble(track); // Скробблим
                    hasScrobbled.set(true); // Устанавливаем флаг
                    logger.info("Scrobbled track: {} by {}", track.getTitle(), track.getArtist());
                }
            }
        };
        mediaPlayer.currentTimeProperty().addListener(progressListener);

        mediaPlayer.setOnReady(() -> {
            mediaPlayer.play();
            scrobbler.updateNowPlaying(track); // Обновляем "сейчас играет" при старте
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            isPlaying.set(false);
            if (!hasScrobbled.get()) {
                scrobbler.scrobble(track); // Если не скробблили — скробблим на конце (если >50%)
            }
            // Здесь можно добавить логику перехода к следующему треку
        });
        notifyTrackChanged();
    }

    public void pause() {
        if (mediaPlayer != null && isPlaying.get()) {
            mediaPlayer.pause();
            isPlaying.set(false);
            savePlayerState(mediaPlayer.getCurrentTime().toSeconds());
        }
    }

    public void resume() {
        if (mediaPlayer == null) {
            logger.info("Пытаюсь запустить с mediaPlayer == null");
            if (playlistService == null) {
                logger.error("playlistService == null");
                return;
            }
            if (playlistService.getTracks() == null) {
                logger.warn("playlistService.getTracks() == null");
                return;
            }
            if (playlistService.getCurrentPlaylist().getTracks().isEmpty()) {
                logger.warn("playlistService.getTracks().isEmpty()");
                return;
            }
            Track track = playlistService.getTracks().getFirst();
            this.play(track);
        }

        if (!isPlaying.get()) {
            mediaPlayer.play();
            isPlaying.set(true);
        } else {
            logger.info("Пытаюсь запустить с нуля");
            Track track = playlistService.getTracks().getFirst();
            this.play(track);
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPlaying.set(false);
            savePlayerState(mediaPlayer.getCurrentTime().toSeconds());
        }
    }

    public void stopAndDisposeIfCurrent(Track track) {
        if (mediaPlayer == null || currentTrack == null || track == null) {
            return;
        }
        if (currentTrack == track || Objects.equals(currentTrack.getFilePath(), track.getFilePath())) {
            disposePlayer();
            isPlaying.set(false);
            currentTrack = null;
            System.gc();
        }
    }

    public void setVolume(double volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume);
        }
    }

    public Track getCurrentTrack() {
        return currentTrack;
    }

    public boolean isCurrentTrack(Track track) {
        if (track == null || currentTrack == null) {
            return false;
        }
        return currentTrack == track || Objects.equals(currentTrack.getFilePath(), track.getFilePath());
    }

    public boolean isPlaying() {
        return isPlaying.get();
    }

    private void savePlayerState(double timeSeconds) {
        if (playerStateService == null || currentTrack == null) {
            return;
        }
        Playlist current = playlistService.getCurrentPlaylist();
        if (current == null) {
            return;
        }
        PlayerState state = new PlayerState(current.getName(), currentTrack.getFilePath(), timeSeconds);
        try {
            playerStateService.saveState(state);
        } catch (IOException e) {
            logger.warn("Не удалось сохранить состояние плеера: {}", e.getMessage());
        }
    }

    public void dispose() {
        disposePlayer();
    }

    private void disposePlayer() {
        if (mediaPlayer != null) {
            if (progressListener != null) {
                mediaPlayer.currentTimeProperty().removeListener(progressListener);
                progressListener = null;
            }
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    public void seek(double v) {
        if (mediaPlayer == null) {
            return;
        }
        double totalMs = mediaPlayer.getTotalDuration().toMillis();
        if (totalMs <= 0) {
            return;
        }
        double targetMs = totalMs * v / 100.0;
        mediaPlayer.seek(new Duration(targetMs));
    }

    public double getCurrentPosition() {
        double duration = mediaPlayer.getTotalDuration().toSeconds();
        double current = mediaPlayer.getCurrentTime().toSeconds();
        double percent =  current / duration * 100;
        return percent;
    }

    public double getDuration() {
        return mediaPlayer.getTotalDuration().toSeconds();
    }


    public void addPlaybackStateListener(Runnable listener) {
        playbackStateListeners.add(listener);
    }


    public void removePlaybackStateListener(Runnable listener) {
        playbackStateListeners.remove(listener);
    }


    public void notifyPlaybackStateChanged(boolean state) {
        for (Runnable listener : playbackStateListeners) {
            Platform.runLater(listener);
        }
    }


}