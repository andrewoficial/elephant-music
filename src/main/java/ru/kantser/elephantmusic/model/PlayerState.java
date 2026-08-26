package ru.kantser.elephantmusic.model;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;

public class PlayerState {
    @Getter
    @Setter
    private String lastPlaylistName;

    @Getter
    @Setter
    private Path lastTrackPath;

    @Getter
    @Setter
    private double lastTimeSeconds;

    public PlayerState() {}

    public PlayerState(String lastPlaylistName, Path lastTrackPath, double lastTimeSeconds) {
        this.lastPlaylistName = lastPlaylistName;
        this.lastTrackPath = lastTrackPath;
        this.lastTimeSeconds = lastTimeSeconds;
    }
}
