package ru.kantser.elephantmusic.service.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kantser.elephantmusic.model.PlayerState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Singleton
public class JacksonPlayerStateService implements PlayerStateService {
    private static final Logger logger = LoggerFactory.getLogger(JacksonPlayerStateService.class);

    private final ObjectMapper objectMapper;
    private final Path statePath;

    @Inject
    public JacksonPlayerStateService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.statePath = Paths.get(System.getProperty("user.home"), ".ElephantPlayer", "player_state.json");
    }

    @Override
    public PlayerState loadState() throws IOException {
        if (Files.exists(statePath)) {
            return objectMapper.readValue(statePath.toFile(), PlayerState.class);
        }
        return getDefaultState();
    }

    @Override
    public void saveState(PlayerState state) throws IOException {
        if (state == null) {
            logger.warn("PlayerState IS NULL");
            return;
        }
        Files.createDirectories(statePath.getParent());
        objectMapper.writeValue(statePath.toFile(), state);
    }

    @Override
    public PlayerState getDefaultState() {
        return new PlayerState(null, null, 0);
    }
}
