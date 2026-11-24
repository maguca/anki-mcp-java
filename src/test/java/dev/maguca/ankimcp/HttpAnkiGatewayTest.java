package dev.maguca.ankimcp;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class HttpAnkiGatewayTest {
    private HttpServer server;
    @AfterEach void stop() { if (server != null) server.stop(0); }

    @Test void sendsVersionSixEnvelopeAndParsesResult() throws Exception {
        var body = new AtomicReference<String>();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/", exchange -> {
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = "{\"result\":[\"Default\"],\"error\":null}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200,response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        var gateway = new HttpAnkiGateway(URI.create("http://127.0.0.1:" + server.getAddress().getPort()), "test-key");
        assertEquals("Default",gateway.invoke("deckNames",Map.of()).get(0).asText());
        assertTrue(body.get().contains("\"version\":6"));
        assertTrue(body.get().contains("\"key\":\"test-key\""));
        assertTrue(body.get().contains("\"action\":\"deckNames\""));
    }

    @Test void surfacesAnkiErrors() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] response = "{\"result\":null,\"error\":\"deck not found\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200,response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        var gateway = new HttpAnkiGateway(URI.create("http://127.0.0.1:" + server.getAddress().getPort()), null);
        assertTrue(assertThrows(AnkiException.class, () -> gateway.invoke("deckNames",Map.of())).getMessage().contains("deck not found"));
    }

    @Test void rejectsNonLoopbackEndpoint() {
        assertThrows(IllegalArgumentException.class, () -> new HttpAnkiGateway(URI.create("http://example.com:8765"),null));
        assertThrows(IllegalArgumentException.class, () -> new HttpAnkiGateway(URI.create("https://127.0.0.1:8765"),null));
    }
}
