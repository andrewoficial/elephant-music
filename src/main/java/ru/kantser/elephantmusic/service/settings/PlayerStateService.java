package ru.kantser.elephantmusic.service.settings;

import ru.kantser.elephantmusic.model.PlayerState;

import java.io.IOException;

public interface PlayerStateService {
    PlayerState loadState() throws IOException;
    void saveState(PlayerState state) throws IOException;
    PlayerState getDefaultState();
}
