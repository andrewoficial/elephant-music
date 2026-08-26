package ru.kantser.elephantmusic.service.identification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kantser.elephantmusic.model.AppSettings;
import ru.kantser.elephantmusic.model.IdentificationResult;
import ru.kantser.elephantmusic.service.settings.SettingsService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Singleton
public class AcrCloudIdentificationProvider implements TrackIdentificationProvider {
    private static final Logger logger = LoggerFactory.getLogger(AcrCloudIdentificationProvider.class);
    private static final int MAX_FRAGMENT_BYTES = 12 * 1024 * 1024;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;
    private final SettingsService settingsService;

    @Inject
    public AcrCloudIdentificationProvider(ObjectMapper objectMapper, SettingsService settingsService) {
        this.objectMapper = objectMapper;
        this.settingsService = settingsService;
    }

    @Override
    public String getName() {
        return "ACRCloud";
    }

    @Override
    public CompletableFuture<IdentificationResult> identify(Path audioFile) {
        return CompletableFuture.supplyAsync(() -> doIdentify(audioFile));
    }

    private IdentificationResult doIdentify(Path audioFile) {
        try {
            AppSettings settings = settingsService.loadSettings();
            String accessKey = settings.getAcrAccessKey();
            String accessSecret = settings.getAcrAccessSecret();
            String host = settings.getAcrHost();

            if (isBlank(accessKey) || isBlank(accessSecret) || isBlank(host)) {
                return IdentificationResult.failure("ACRCloud не настроен: укажите access_key, access_secret и host в настройках");
            }

            byte[] fragment = IdentificationHttpUtils.readFragment(audioFile, MAX_FRAGMENT_BYTES);
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String signature = sign(accessKey, accessSecret, timestamp);

            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("sample_bytes", String.valueOf(fragment.length));
            fields.put("access_key", accessKey);
            fields.put("data_type", "audio");
            fields.put("signature_version", "1");
            fields.put("signature", signature);
            fields.put("timestamp", timestamp);

            String url = "https://" + host + "/v1/identify";
            String response = IdentificationHttpUtils.postMultipart(httpClient, url, fields,
                    "sample", "fragment.mp3", "audio/mpeg", fragment);
            return parse(response);
        } catch (Exception e) {
            logger.error("Ошибка распознавания ACRCloud: {}", audioFile, e);
            return IdentificationResult.failure("Ошибка ACRCloud: " + e.getMessage());
        }
    }

    private String sign(String accessKey, String accessSecret, String timestamp) throws Exception {
        String signatureString = "POST\n/v1/identify\n" + accessKey + "\naudio\n1\n" + timestamp;
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(accessSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        byte[] raw = mac.doFinal(signatureString.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(raw);
    }

    private IdentificationResult parse(String json) throws IOException {
        JsonNode root = objectMapper.readTree(json);
        int code = root.path("status").path("code").asInt(-1);
        if (code == 0) {
            JsonNode music = root.path("metadata").path("music");
            if (music.isArray() && !music.isEmpty()) {
                JsonNode first = music.get(0);
                String title = first.path("title").asText();
                String artist = "";
                JsonNode artists = first.path("artists");
                if (artists.isArray() && !artists.isEmpty()) {
                    artist = artists.get(0).path("name").asText();
                }
                String album = first.path("album").path("name").asText();
                return IdentificationResult.success(title, artist, album);
            }
            return IdentificationResult.failure("Трек не найден");
        }
        String message = root.path("status").path("msg").asText("Ошибка ACRCloud");
        return IdentificationResult.failure(message);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
