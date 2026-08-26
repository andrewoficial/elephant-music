package ru.kantser.elephantmusic.service.settings;

import ru.kantser.elephantmusic.model.Playlist;

import java.io.IOException;
import java.util.List;

public interface PlayListSaverService {
    List<Playlist> loadPlayLists() throws IOException;
    void savePlayLists(List<Playlist> playlists) throws IOException;
    Playlist getDefaultPlayList();
}
