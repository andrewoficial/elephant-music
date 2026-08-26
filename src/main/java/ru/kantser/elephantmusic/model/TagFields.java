package ru.kantser.elephantmusic.model;

import lombok.Getter;
import lombok.Setter;

public class TagFields {
    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private String artist;

    @Getter
    @Setter
    private String album;

    @Getter
    @Setter
    private String year;

    @Getter
    @Setter
    private String track;

    @Getter
    @Setter
    private String comment;

    @Getter
    @Setter
    private String genre;

    @Getter
    @Setter
    private String composer;
}
