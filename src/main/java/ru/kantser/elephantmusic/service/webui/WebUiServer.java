package ru.kantser.elephantmusic.service.webui;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kantser.elephantmusic.model.Track;
import ru.kantser.elephantmusic.service.AudioPlayerService;
import ru.kantser.elephantmusic.service.PlaylistService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

@Singleton
public class WebUiServer {
    private static final Logger logger = LoggerFactory.getLogger(WebUiServer.class);

    private static final int DEFAULT_PORT = 8080;

    private final AudioPlayerService audioPlayerService;
    private final PlaylistService playlistService;
    private final int port;
    private HttpServer server;

    @Inject
    public WebUiServer(AudioPlayerService audioPlayerService, PlaylistService playlistService) {
        this.audioPlayerService = audioPlayerService;
        this.playlistService = playlistService;
        this.port = Integer.getInteger("elephant.web.port", DEFAULT_PORT);
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", this::serveIndex);
            server.createContext("/api/status", this::handleStatus);
            server.createContext("/api/toggle", this::handleToggle);
            server.createContext("/api/next", this::handleNext);
            server.createContext("/api/prev", this::handlePrev);
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            logger.info("Web-пульт запущен: http://localhost:{}", port);
        } catch (IOException e) {
            logger.warn("Не удалось запустить Web-пульт на порту {}: {}", port, e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public int getPort() {
        return port;
    }

    private void serveIndex(HttpExchange exchange) throws IOException {
        byte[] html = readResource("/webui/index.html");
        if (html == null) {
            sendText(exchange, 404, "text/plain; charset=utf-8", "index.html not found");
            return;
        }
        sendBytes(exchange, 200, "text/html; charset=utf-8", html);
    }

    private void handleStatus(HttpExchange exchange) throws IOException {
        boolean playing = audioPlayerService.isPlaying();
        Track track = audioPlayerService.getCurrentTrack();
        String title = track == null ? "" : track.getTitle();
        String artist = track == null ? "" : track.getArtist();

        String json = "{"
                + "\"playing\":" + playing + ","
                + "\"title\":\"" + escapeJson(title) + "\","
                + "\"artist\":\"" + escapeJson(artist) + "\""
                + "}";
        sendText(exchange, 200, "application/json; charset=utf-8", json);
    }

    private void handleToggle(HttpExchange exchange) throws IOException {
        Platform.runLater(() -> {
            if (audioPlayerService.isPlaying()) {
                audioPlayerService.pause();
            } else {
                audioPlayerService.resume();
            }
        });
        sendText(exchange, 200, "application/json; charset=utf-8", "{\"ok\":true}");
    }

    private void handleNext(HttpExchange exchange) throws IOException {
        Platform.runLater(() -> {
            Track next = playlistService.getNextTrack();
            if (next != null) {
                audioPlayerService.play(next);
            }
        });
        sendText(exchange, 200, "application/json; charset=utf-8", "{\"ok\":true}");
    }

    private void handlePrev(HttpExchange exchange) throws IOException {
        Platform.runLater(() -> {
            Track prev = playlistService.getPreviousTrack();
            if (prev != null) {
                audioPlayerService.play(prev);
            }
        });
        sendText(exchange, 200, "application/json; charset=utf-8", "{\"ok\":true}");
    }

    private byte[] readResource(String path) {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in == null) {
                return null;
            }
            return in.readAllBytes();
        } catch (IOException e) {
            logger.warn("Не удалось прочитать ресурс {}: {}", path, e.getMessage());
            return null;
        }
    }

    private void sendText(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        sendBytes(exchange, status, contentType, body.getBytes(StandardCharsets.UTF_8));
    }

    private void sendBytes(HttpExchange exchange, int status, String contentType, byte[] body) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
