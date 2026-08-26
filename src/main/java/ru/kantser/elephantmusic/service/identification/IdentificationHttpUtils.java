package ru.kantser.elephantmusic.service.identification;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

final class IdentificationHttpUtils {
    private IdentificationHttpUtils() {
    }

    static byte[] readFragment(Path path, int maxBytes) throws IOException {
        long size = Files.size(path);
        int length = (int) Math.min(size, maxBytes);
        byte[] data = new byte[length];
        try (InputStream in = Files.newInputStream(path)) {
            int read = in.read(data);
            if (read < length) {
                byte[] trimmed = new byte[read];
                System.arraycopy(data, 0, trimmed, 0, read);
                return trimmed;
            }
        }
        return data;
    }

    static String postMultipart(HttpClient client, String url, Map<String, String> textFields,
                                String fileField, String filename, String contentType, byte[] file)
            throws IOException, InterruptedException {
        String boundary = "----ElephantMusicBoundary" + System.nanoTime();
        ByteArrayOutputStream body = new ByteArrayOutputStream();

        for (Map.Entry<String, String> entry : textFields.entrySet()) {
            body.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
            body.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            body.write((entry.getValue() + "\r\n").getBytes(StandardCharsets.UTF_8));
        }

        body.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(("Content-Disposition: form-data; name=\"" + fileField + "\"; filename=\"" + filename + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(file);
        body.write("\r\n".getBytes(StandardCharsets.UTF_8));
        body.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
