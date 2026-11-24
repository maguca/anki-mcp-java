package dev.maguca.ankimcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/** Narrow HTTP adapter for the AnkiConnect legacy v6 envelope. */
public final class HttpAnkiGateway implements AnkiGateway {
    private static final int MAX_RESPONSE_BYTES = 4 * 1024 * 1024;
    private final URI endpoint;
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final String apiKey;

    public HttpAnkiGateway(URI endpoint, String apiKey) {
        this(endpoint, apiKey, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build(), new ObjectMapper());
    }

    HttpAnkiGateway(URI endpoint, String apiKey, HttpClient client, ObjectMapper mapper) {
        this.endpoint = Objects.requireNonNull(endpoint);
        if (!"http".equalsIgnoreCase(endpoint.getScheme()) || endpoint.getUserInfo() != null
                || endpoint.getHost() == null || !isLoopback(endpoint.getHost())
                || (endpoint.getRawPath() != null && !endpoint.getRawPath().isEmpty() && !"/".equals(endpoint.getRawPath()))
                || endpoint.getRawQuery() != null || endpoint.getRawFragment() != null) {
            throw new IllegalArgumentException("ANKI_CONNECT_URL must be a loopback HTTP root URL");
        }
        this.client = Objects.requireNonNull(client);
        this.mapper = Objects.requireNonNull(mapper);
        this.apiKey = apiKey;
    }

    private static boolean isLoopback(String host) {
        return "127.0.0.1".equals(host) || "localhost".equalsIgnoreCase(host) || "[::1]".equals(host);
    }

    @Override public JsonNode invoke(String action, Map<String, ?> params) {
        try {
            var payload = new java.util.LinkedHashMap<String, Object>();
            payload.put("action", action);
            payload.put("version", 6);
            payload.put("params", params);
            if (apiKey != null && !apiKey.isBlank()) payload.put("key", apiKey);
            HttpRequest request = HttpRequest.newBuilder(endpoint)
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(mapper.writeValueAsBytes(payload))).build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) throw new AnkiException("AnkiConnect returned HTTP " + response.statusCode());
            if (response.body().length > MAX_RESPONSE_BYTES) throw new AnkiException("AnkiConnect response exceeds 4 MiB");
            JsonNode envelope = mapper.readTree(response.body());
            if (!envelope.isObject() || !envelope.has("error") || !envelope.has("result"))
                throw new AnkiException("Invalid AnkiConnect response envelope");
            if (!envelope.get("error").isNull()) throw new AnkiException("AnkiConnect: " + envelope.get("error").asText());
            return envelope.get("result");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AnkiException("AnkiConnect request interrupted", e);
        } catch (IOException e) {
            throw new AnkiException("Cannot reach AnkiConnect at " + endpoint + "; open Anki and enable AnkiConnect", e);
        }
    }
}
