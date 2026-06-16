package ru.kantser.elephantmusic.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class AppSettings {
    @JsonProperty("language")
    @Getter @Setter
    private String language;
    
    @JsonProperty("lastFmName")
    @Getter @Setter
    private String lastFmName;
    
    @JsonProperty("lastFmToken")
    @Getter @Setter
    private String lastFmToken;

    @JsonProperty("activeScrobbling")
    @Getter @Setter
    private Boolean activeScrobbling;

    @JsonProperty("updateSourceUrl")
    @Getter @Setter
    private String updateSourceUrl;

    @JsonProperty("updateToken")
    @Getter @Setter
    private String updateToken;

}