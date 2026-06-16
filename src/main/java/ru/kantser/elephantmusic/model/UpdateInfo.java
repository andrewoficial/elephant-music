package ru.kantser.elephantmusic.model;

public class UpdateInfo {
    private final String version;
    private final String downloadUrl;
    private final String releaseNotes;

    public UpdateInfo(String version, String downloadUrl, String releaseNotes) {
        this.version = version;
        this.downloadUrl = downloadUrl;
        this.releaseNotes = releaseNotes;
    }

    public String getVersion() {
        return version;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }
}
