package ru.kantser.elephantmusic.service.identification;

import ru.kantser.elephantmusic.model.IdentificationResult;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class AcoustIdIdentificationProvider implements TrackIdentificationProvider {
    @Override
    public String getName() {
        return "AcoustID";
    }

    @Override
    public CompletableFuture<IdentificationResult> identify(Path audioFile) {
        return CompletableFuture.completedFuture(
                IdentificationResult.failure("Провайдер AcoustID ещё не реализован"));
    }
}
