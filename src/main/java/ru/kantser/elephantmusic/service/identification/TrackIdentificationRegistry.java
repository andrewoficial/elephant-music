package ru.kantser.elephantmusic.service.identification;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Singleton
public class TrackIdentificationRegistry {
    private final List<TrackIdentificationProvider> providers;

    @Inject
    public TrackIdentificationRegistry(Set<TrackIdentificationProvider> providers) {
        this.providers = new ArrayList<>(providers);
        this.providers.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
    }

    public List<TrackIdentificationProvider> getProviders() {
        return providers;
    }
}
