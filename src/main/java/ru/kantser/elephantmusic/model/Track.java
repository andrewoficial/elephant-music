package ru.kantser.elephantmusic.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Track {
    // Getters and setters
    @Getter
    @Setter
    private String title;
    @Getter
    @Setter
    private String artist;
    @Getter
    @Setter
    private Path filePath;
    @Getter
    @Setter
    private Duration duration;

    @Getter
    @Setter
    private Integer rating;

    @Getter
    @Setter
    private List<String> tags;

    @Getter
    @Setter
    private Path coverArtPath;

    @Getter
    @Setter
    private String lyrics;

    // Конструктор без аргументов для Jackson
    public Track() {}

    public Track(String title, String artist, Path filePath, Duration duration) {
        this.title = title;
        this.artist = artist;
        this.filePath = filePath;
        this.duration = duration;
    }

    @Override
    public String toString() {
        return artist + " - " + title;
    }
}