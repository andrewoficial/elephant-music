package ru.kantser.elephantmusic.model;

import lombok.Getter;
import lombok.Setter;

public class IdentificationResult {
    @Getter
    @Setter
    private boolean success;

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
    private String errorMessage;

    public static IdentificationResult success(String title, String artist, String album) {
        IdentificationResult result = new IdentificationResult();
        result.setSuccess(true);
        result.setTitle(title);
        result.setArtist(artist);
        result.setAlbum(album);
        return result;
    }

    public static IdentificationResult failure(String errorMessage) {
        IdentificationResult result = new IdentificationResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        return result;
    }
}
