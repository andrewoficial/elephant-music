package ru.kantser.elephantmusic.service.identification;

import ru.kantser.elephantmusic.model.IdentificationResult;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface TrackIdentificationProvider {
    String getName();

    CompletableFuture<IdentificationResult> identify(Path audioFile);
}
