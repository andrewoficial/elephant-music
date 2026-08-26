package ru.kantser.elephantmusic.service.identification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kantser.elephantmusic.model.IdentificationResult;
import ru.kantser.elephantmusic.service.settings.SettingsService;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Singleton
public class AudDIdentificationProvider implements TrackIdentificationProvider {
    private static final Logger logger = LoggerFactory.getLogger(AudDIdentificationProvider.class);
    private static final String API_URL = "https://api.audd.io/";
    private static final int MAX_FRAGMENT_BYTES = 12 * 1024 * 1024;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;
    private final SettingsService settingsService;

    @Inject
    public AudDIdentificationProvider(ObjectMapper objectMapper, SettingsService settingsService) {
        this.objectMapper = objectMapper;
        this.settingsService = settingsService;
    }

    @Override
    public String getName() {
        return "AudD";
    }

    @Override
    public CompletableFuture<IdentificationResult> identify(Path audioFile) {
        return CompletableFuture.supplyAsync(() -> doIdentify(audioFile));
    }

    private IdentificationResult doIdentify(Path audioFile) {
        try {
            String token = settingsService.loadSettings().getAuddToken();
            if (token == null || token.isBlank()) {
                return IdentificationResult.failure("Не задан API-ключ AudD (auddToken в настройках)");
            }
            byte[] fragment = IdentificationHttpUtils.readFragment(audioFile, MAX_FRAGMENT_BYTES);
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("api_token", token);
            String response = IdentificationHttpUtils.postMultipart(httpClient, API_URL, fields,
                    "file", "fragment.mp3", "audio/mpeg", fragment);
            return parse(response);
        } catch (Exception e) {
            logger.error("Ошибка распознавания AudD: {}", audioFile, e);
            return IdentificationResult.failure("Ошибка AudD: " + e.getMessage());
        }
    }

    private IdentificationResult parse(String json) throws IOException {
        JsonNode root = objectMapper.readTree(json);
        if ("success".equals(root.path("status").asText())) {
            JsonNode result = root.path("result");
            return IdentificationResult.success(
                    result.path("title").asText(),
                    result.path("artist").asText(),
                    result.path("album").asText());
        }
        String message = root.path("error").path("error_message").asText("Не удалось распознать трек");
        return IdentificationResult.failure(message);
    }
}
