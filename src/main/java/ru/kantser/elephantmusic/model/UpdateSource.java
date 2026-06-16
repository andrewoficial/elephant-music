package ru.kantser.elephantmusic.model;

public class UpdateSource {
    private final String name;
    private final String repoUrl;

    public UpdateSource(String name, String repoUrl) {
        this.name = name;
        this.repoUrl = repoUrl;
    }

    public String getName() {
        return name;
    }

    public String getRepoUrl() {
        return repoUrl;
    }
}
