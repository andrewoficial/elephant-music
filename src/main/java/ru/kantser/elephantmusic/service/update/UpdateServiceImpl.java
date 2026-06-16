package ru.kantser.elephantmusic.service.update;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kantser.elephantmusic.model.AppSettings;
import ru.kantser.elephantmusic.model.UpdateInfo;
import ru.kantser.elephantmusic.model.UpdateSource;
import ru.kantser.elephantmusic.service.settings.SettingsService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Singleton
public class UpdateServiceImpl implements UpdateService {
    private static final Logger log = LoggerFactory.getLogger(UpdateServiceImpl.class);

    private static final String GITHUB_URL = "https://github.com/andrewoficial/ElephantMusic";
    private static final String GITEA_URL = "http://192.168.1.162:3000/Andrew/ElephantMusic";

    private final ApplicationInfoService appInfo;
    private final SettingsService settingsService;
    private final ObjectMapper objectMapper;
    private String currentRepoUrl;

    @Inject
    public UpdateServiceImpl(ApplicationInfoService appInfo,
                             SettingsService settingsService,
                             ObjectMapper objectMapper) {
        this.appInfo = appInfo;
        this.settingsService = settingsService;
        this.objectMapper = objectMapper;
        log.debug("UpdateServiceImpl created");
    }

    @Override
    public List<UpdateSource> getSources() {
        List<UpdateSource> sources = new ArrayList<>();
        sources.add(new UpdateSource("GitHub", GITHUB_URL));
        sources.add(new UpdateSource("Gitea", GITEA_URL));

        AppSettings settings = null;
        try {
            settings = settingsService.loadSettings();
        } catch (IOException e) {
            log.warn("Failed to load settings", e);
            return null;
        }
        String userSource = settings.getUpdateSourceUrl();
        if (userSource != null && !userSource.isBlank()) {
            String normalized = userSource.trim();
            if (!normalized.contains("://"))
                normalized = "http://" + normalized;
            sources.add(new UpdateSource("Пользовательский", normalized));
        }
        return sources;
    }

    private String resolveToken(String repoUrl) throws IOException {
        AppSettings settings = settingsService.loadSettings();
        if (repoUrl.contains("github.com"))
            return null;
        String token = settings.getUpdateToken();
        if (token == null || token.isBlank())
            return "abfd3998bd9315e2bf23940216ed287f877f0139";
        return token;
    }

    @Override
    public CompletableFuture<UpdateInfo> checkForUpdates(UpdateSource source) {
        log.info("=== Check updates for source: {} ===", source.getName());
        return CompletableFuture.supplyAsync(() -> {
            try {
                String repoUrl = source.getRepoUrl();
                log.info("Repo URL: {}", repoUrl);
                currentRepoUrl = repoUrl;

                String apiUrl = buildApiUrl(repoUrl);
                log.info("API URL: {}", apiUrl);

                String token = resolveToken(repoUrl);
                boolean hasToken = token != null && !token.isEmpty();
                log.info("Token {}used", hasToken ? "" : "NOT ");

                JsonNode releases = fetchReleases(apiUrl, token);
                log.info("Releases count: {}", releases.size());
                if (releases.isEmpty()) {
                    log.info("No releases found");
                    return null;
                }

                JsonNode latestRelease = releases.get(0);
                String latestVersion = extractVersion(latestRelease);
                log.info("Latest version: {}", latestVersion);

                String currentVersion = appInfo.getVersion();
                log.info("Current version: {}", currentVersion);

                if (!isNewer(latestVersion, currentVersion)) {
                    log.info("No updates available");
                    return null;
                }

                StringBuilder aggregatedNotes = new StringBuilder();
                String downloadUrl = null;
                int releasesCounted = 0;

                for (JsonNode rel : releases) {
                    String tag = extractVersion(rel);
                    if (!isNewer(tag, currentVersion)) break;
                    releasesCounted++;

                    String body = rel.has("body") ? rel.get("body").asText("") : "";
                    if (body != null && !body.isEmpty()) {
                        if (aggregatedNotes.length() > 0)
                            aggregatedNotes.append("\n\n");
                        aggregatedNotes.append("## ").append(tag).append("\n").append(body);
                    }
                    if (downloadUrl == null)
                        downloadUrl = extractDownloadUrl(rel);
                }
                log.info("Aggregated {} new releases", releasesCounted);

                if (downloadUrl == null) {
                    log.warn("No download file found");
                    return null;
                }

                return new UpdateInfo(latestVersion, downloadUrl, aggregatedNotes.toString());

            } catch (Exception e) {
                log.error("Error checking updates: {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Path> downloadAndPrepareUpdate(UpdateInfo updateInfo, Consumer<Integer> progressCallback) {
        log.info("=== Download update: start ===");
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path tempFile = Files.createTempFile("elephantmusic_update_", ".jar");
                log.info("Download from {} to {}", updateInfo.getDownloadUrl(), tempFile);
                String token = currentRepoUrl != null ? resolveToken(currentRepoUrl) : null;
                downloadFile(updateInfo.getDownloadUrl(), tempFile, token, progressCallback);
                long size = Files.size(tempFile);
                log.info("Download complete. Size: {} bytes", size);
                return tempFile;
            } catch (Exception e) {
                log.error("Error downloading: {}", e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void applyUpdate(Path newJarPath) {
        log.info("=== Apply update ===");
        try {
            String currentVersion = appInfo.getVersion();
            Path appDataDir = getAppDataDir();
            Path backupDir = appDataDir.resolve("backup").resolve(currentVersion);
            log.info("Backup dir: {}", backupDir);
            Files.createDirectories(backupDir);

            Path configFile = appDataDir.resolve("settings.json");
            if (Files.exists(configFile))
                Files.copy(configFile, backupDir.resolve("settings.json"), StandardCopyOption.REPLACE_EXISTING);

            Path logsDir = appDataDir.resolve("logs");
            if (Files.exists(logsDir)) {
                Path backupLogsDir = backupDir.resolve("logs");
                Files.createDirectories(backupLogsDir);
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(logsDir)) {
                    for (Path entry : stream) {
                        try {
                            Files.copy(entry, backupLogsDir.resolve(entry.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                        } catch (Exception e) {
                            log.warn("Failed to copy log {}: {}", entry.getFileName(), e.getMessage());
                        }
                    }
                }
            }

            Path currentJar = getCurrentJarPath();
            log.info("Current JAR: {}", currentJar);
            log.info("New JAR: {}", newJarPath);

            boolean moved = false;
            try {
                Path backupJar = backupDir.resolve("ElephantMusic-" + currentVersion + ".jar");
                Files.move(currentJar, backupJar, StandardCopyOption.REPLACE_EXISTING);
                moved = true;
                log.info("Current JAR moved to backup: {}", backupJar);
            } catch (Exception e) {
                log.warn("Failed to move current JAR: {}", e.getMessage());
            }

            if (moved) {
                Files.copy(newJarPath, currentJar, StandardCopyOption.REPLACE_EXISTING);
                log.info("New JAR copied: {}", currentJar);
                restartApplication(currentJar);
            } else {
                Path batchFile = appDataDir.resolve("update.bat");
                String jarPath = currentJar.toString();
                String backupJarPath = backupDir.resolve("ElephantMusic-" + currentVersion + ".jar").toString();
                String batchContent =
                        "@echo off\r\n" +
                        "title Updating ElephantMusic...\r\n" +
                        "echo.\r\n" +
                        "echo [1/6] Backing up current version...\r\n" +
                        "if not exist \"" + backupDir.toString() + "\" mkdir \"" + backupDir.toString() + "\"\r\n" +
                        "copy /Y \"" + jarPath + "\" \"" + backupJarPath + "\" >NUL\r\n" +
                        "echo      OK\r\n" +
                        "echo.\r\n" +
                        "echo [2/6] Waiting for running instance to exit...\r\n" +
                        ":wait\r\n" +
                        "timeout /T 2 /NOBREAK >NUL\r\n" +
                        "tasklist /FI \"IMAGENAME eq java.exe\" 2>NUL | find /I \"java.exe\" >NUL\r\n" +
                        "if \"%ERRORLEVEL%\"==\"0\" goto wait\r\n" +
                        "echo      OK\r\n" +
                        "echo.\r\n" +
                        "echo [3/6] Copying new version...\r\n" +
                        "copy /Y \"" + newJarPath.toString() + "\" \"" + jarPath + "\" >NUL\r\n" +
                        "echo      OK\r\n" +
                        "echo.\r\n" +
                        "echo [4/6] Starting updated application...\r\n" +
                        "start \"\" javaw -jar \"" + jarPath + "\"\r\n" +
                        "echo      OK\r\n" +
                        "echo.\r\n" +
                        "echo [5/6] Cleaning up temporary files...\r\n" +
                        "del /Q \"" + newJarPath.toString() + "\" >NUL 2>&1\r\n" +
                        "echo      OK\r\n" +
                        "echo.\r\n" +
                        "echo ============================================\r\n" +
                        "echo   ElephantMusic has been updated successfully!\r\n" +
                        "echo    You may close this window.\r\n" +
                        "echo ============================================\r\n" +
                        "timeout /T 10 /NOBREAK >NUL\r\n" +
                        "exit\r\n";
                Files.writeString(batchFile, batchContent);
                log.info("Created batch script: {}", batchFile);

                new ProcessBuilder("cmd.exe", "/c", "start", "/MIN", "\"\"", batchFile.toString())
                        .start();
                log.info("Batch script launched, exiting application...");
                System.exit(0);
            }
        } catch (Exception e) {
            log.error("Failed to apply update", e);
            throw new RuntimeException("Failed to apply update: " + e.getMessage(), e);
        }
    }

    // --- Helpers ---

    private String buildApiUrl(String repoUrl) {
        if (repoUrl.endsWith("/"))
            repoUrl = repoUrl.substring(0, repoUrl.length() - 1);

        if (repoUrl.contains("/api/v1/repos/") || repoUrl.contains("api.github.com/repos/"))
            return repoUrl;

        if (repoUrl.contains("github.com")) {
            String[] parts = repoUrl.split("/");
            if (parts.length >= 5) {
                String owner = parts[parts.length - 2];
                String repo = parts[parts.length - 1];
                return "https://api.github.com/repos/" + owner + "/" + repo + "/releases";
            }
        }

        if (repoUrl.contains("://")) {
            String withoutProtocol = repoUrl.substring(repoUrl.indexOf("://") + 3);
            int firstSlash = withoutProtocol.indexOf('/');
            if (firstSlash != -1) {
                String host = withoutProtocol.substring(0, firstSlash);
                String repoPath = withoutProtocol.substring(firstSlash + 1);
                String[] pathParts = repoPath.split("/");
                if (pathParts.length >= 2) {
                    String owner = pathParts[0];
                    String repo = pathParts[1];
                    return "http://" + host + "/api/v1/repos/" + owner + "/" + repo + "/releases";
                }
            }
        }
        return repoUrl;
    }

    private JsonNode fetchReleases(String apiUrl, String token) throws IOException, URISyntaxException {
        log.debug("HTTP GET {}", apiUrl);
        URL url = new URI(apiUrl).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "ElephantMusic-UpdateChecker");
        if (token != null && !token.isEmpty())
            conn.setRequestProperty("Authorization", "token " + token);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        int responseCode = conn.getResponseCode();
        log.debug("Response code: {}", responseCode);
        if (responseCode == 403)
            throw new IOException("Access denied. Check token in settings.");
        if (responseCode != 200) {
            String errorBody = "";
            try (InputStream es = conn.getErrorStream()) {
                if (es != null) errorBody = readString(es);
            }
            throw new IOException("Server returned " + responseCode + ": " + errorBody);
        }

        String json = readString(conn.getInputStream());
        log.debug("JSON response length: {}", json.length());
        return objectMapper.readTree(json);
    }

    private String extractVersion(JsonNode release) {
        String tag = release.has("tag_name") ? release.get("tag_name").asText("") : "";
        if (tag.startsWith("v"))
            tag = tag.substring(1);
        return tag;
    }

    private String extractDownloadUrl(JsonNode release) {
        JsonNode assets = release.get("assets");
        String firstJarUrl = null;
        if (assets != null && assets.isArray()) {
            for (JsonNode asset : assets) {
                String name = asset.has("name") ? asset.get("name").asText("") : "";
                if (name != null && name.endsWith(".jar")) {
                    String apiUrl = asset.has("url") ? asset.get("url").asText("") : null;
                    String browserUrl = asset.has("browser_download_url") ? asset.get("browser_download_url").asText("") : null;
                    log.info("Found asset: name={}, url={}, browser_download_url={}", name, apiUrl, browserUrl);
                    if (name.contains("ElephantMusic") || name.contains("elephant-music")) {
                        return apiUrl != null ? apiUrl : browserUrl;
                    }
                    if (firstJarUrl == null)
                        firstJarUrl = apiUrl != null ? apiUrl : browserUrl;
                }
            }
        }
        if (firstJarUrl != null) {
            log.info("Specific JAR not found, using first JAR: {}", firstJarUrl);
            return firstJarUrl;
        }
        String zipball = release.has("zipball_url") ? release.get("zipball_url").asText("") : null;
        log.info("No JAR asset, fallback to zipball: {}", zipball);
        return zipball;
    }

    private String readString(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line);
        }
        return sb.toString();
    }

    private boolean isNewer(String newVersion, String currentVersion) {
        if (newVersion == null || currentVersion == null)
            return false;
        int[] v1 = parseVersion(newVersion);
        int[] v2 = parseVersion(currentVersion);
        for (int i = 0; i < 3; i++) {
            if (v1[i] > v2[i]) return true;
            if (v1[i] < v2[i]) return false;
        }
        return false;
    }

    private int[] parseVersion(String version) {
        int[] result = {0, 0, 0};
        String[] parts = version.split("\\.");
        for (int i = 0; i < Math.min(parts.length, 3); i++) {
            try {
                result[i] = Integer.parseInt(parts[i].replaceAll("[^0-9]", ""));
            } catch (NumberFormatException ignored) {}
        }
        return result;
    }

    private void downloadFile(String urlStr, Path destination, String token, Consumer<Integer> progressCallback) throws IOException, URISyntaxException {
        URL url = new URI(urlStr).toURL();
        boolean hasToken = token != null && !token.isEmpty();
        log.info("Download URL: {}", urlStr);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        conn.setRequestProperty("Accept", "application/octet-stream");
        if (hasToken)
            conn.setRequestProperty("Authorization", "token " + token);
        conn.setInstanceFollowRedirects(false);

        int responseCode = conn.getResponseCode();
        int redirectCount = 0;
        while ((responseCode == 301 || responseCode == 302 || responseCode == 303 || responseCode == 307) && redirectCount < 5) {
            String location = conn.getHeaderField("Location");
            log.info("Redirect to: {}", location);
            url = new URI(location).toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            conn.setRequestProperty("Accept", "application/octet-stream");
            if (hasToken)
                conn.setRequestProperty("Authorization", "token " + token);
            conn.setInstanceFollowRedirects(false);
            responseCode = conn.getResponseCode();
            redirectCount++;
        }

        if (responseCode != 200) {
            String errorBody = "";
            try (InputStream es = conn.getErrorStream()) {
                if (es != null) errorBody = readString(es);
            }
            throw new IOException("Server returned " + responseCode + ": " + errorBody);
        }

        int contentLength = conn.getContentLength();
        try (InputStream in = conn.getInputStream();
             OutputStream out = Files.newOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int read;
            long totalRead = 0;
            int lastPercent = -1;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                totalRead += read;
                if (contentLength > 0 && progressCallback != null) {
                    int percent = (int) (totalRead * 100 / contentLength);
                    if (percent != lastPercent) {
                        lastPercent = percent;
                        progressCallback.accept(Math.min(percent, 100));
                    }
                }
            }
        }
    }

    private Path getCurrentJarPath() throws Exception {
        URI uri = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
        return Paths.get(uri);
    }

    private Path getAppDataDir() {
        String base = System.getenv("APPDATA");
        if (base == null || base.isBlank())
            base = System.getProperty("user.home", ".");
        return Paths.get(base, ".ElephantPlayer");
    }

    private void restartApplication(Path jarPath) {
        log.info("Restarting: {}", jarPath);
        try {
            new ProcessBuilder("javaw", "-jar", jarPath.toString()).start();
            System.exit(0);
        } catch (Exception e) {
            log.error("Failed to restart", e);
        }
    }
}
