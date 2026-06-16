package ru.kantser.elephantmusic.service.update;

import ru.kantser.elephantmusic.model.UpdateInfo;
import ru.kantser.elephantmusic.model.UpdateSource;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface UpdateService {
    List<UpdateSource> getSources();

    CompletableFuture<UpdateInfo> checkForUpdates(UpdateSource source);

    CompletableFuture<Path> downloadAndPrepareUpdate(UpdateInfo updateInfo, Consumer<Integer> progressCallback);

    void applyUpdate(Path newJarPath);
}
