package com.cgi.kpi.dashboard.ai.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.cgi.kpi.dashboard.ai.config.AiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

class HttpGeminiApiTransportTest {

    private static final String MODEL = "gemini-2.5-flash";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void failsClearlyWhenApiKeyMissing() {
        AiProperties properties = geminiProperties(9999);
        properties.setApiKey(" ");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> new HttpGeminiApiTransport(properties, objectMapper));
        assertTrue(ex.getMessage().contains("GEMINI_API_KEY"));
    }

    @Test
    void failsClearlyWhenGeminiModelMissing() {
        AiProperties properties = geminiProperties(9999);
        properties.setApiKey("test-key");
        properties.setModel("local-mock");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> new HttpGeminiApiTransport(properties, objectMapper));
        assertTrue(ex.getMessage().contains("APP_AI_MODEL"));
    }

    @Test
    void buildsHtmlCompatibleRequestBody() throws Exception {
        String json = objectMapper.writeValueAsString(HttpGeminiApiTransport.buildRequestBody("prompt"));
        JsonNode root = objectMapper.readTree(json);

        assertTrue(root.path("contents").isArray());
        assertEquals("prompt", root.path("contents").path(0).path("parts").path(0).path("text").asText());
        assertTrue(root.path("contents").path(0).path("role").isMissingNode());
        assertTrue(root.path("generationConfig").isMissingNode());
    }

    @Test
    void buildsHtmlCompatibleUrlForGemini25Flash() {
        assertEquals(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent",
                HttpGeminiApiTransport.buildGenerateContentUri(
                                MODEL, "https://generativelanguage.googleapis.com")
                        .toString());
    }

    @Test
    void setsApiKeyHeaderWhenKeyPresent() throws Exception {
        AtomicReference<String> capturedKey = new AtomicReference<>();
        AtomicReference<String> capturedBody = new AtomicReference<>();
        HttpServer server = createServer("/v1beta/models/" + MODEL + ":generateContent", exchange -> {
            capturedKey.set(exchange.getRequestHeaders().getFirst("x-goog-api-key"));
            try {
                capturedBody.set(readBody(exchange));
            } catch (Exception ex) {
                throw new UncheckedIOException(ex instanceof java.io.IOException io ? io : new java.io.IOException(ex));
            }
            writeJson(exchange, 200, successBody("{}"));
        });

        try {
            AiProperties properties = geminiProperties(server.getAddress().getPort());
            properties.setApiKey("test-key");
            properties.setModel(MODEL);

            HttpGeminiApiTransport transport = new HttpGeminiApiTransport(properties, objectMapper);
            String response = transport.generateJson("prompt");

            assertEquals("{}", response);
            assertEquals("test-key", capturedKey.get());
            JsonNode body = objectMapper.readTree(capturedBody.get());
            assertEquals("prompt", body.path("contents").path(0).path("parts").path(0).path("text").asText());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void concatenatesAllResponseParts() throws Exception {
        String responseBody =
                """
                        {"candidates":[{"content":{"parts":[{"text":"{\\"ok\\":"},{"text":"true}"}]}}]}
                        """;
        assertEquals("{\"ok\":true}", HttpGeminiApiTransport.extractText(responseBody, objectMapper));
    }

    @Test
    void mapsProvider403ToTransportException() throws Exception {
        HttpServer server = createServer("/v1beta/models/" + MODEL + ":generateContent", exchange -> writeJson(
                exchange,
                403,
                """
                        {"error":{"code":403,"message":"API key not valid","status":"PERMISSION_DENIED"}}
                        """));

        try {
            AiProperties properties = geminiProperties(server.getAddress().getPort());
            properties.setApiKey("test-key");
            properties.setModel(MODEL);

            HttpGeminiApiTransport transport = new HttpGeminiApiTransport(properties, objectMapper);
            GeminiTransportException ex =
                    assertThrows(GeminiTransportException.class, () -> transport.generateJson("prompt"));

            assertEquals(403, ex.getHttpStatus());
            assertTrue(ex.getMessage().contains("403"));
            assertTrue(ex.getMessage().contains("PERMISSION_DENIED"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void mapsProvider404ToTransportException() throws Exception {
        HttpServer server = createServer("/v1beta/models/unknown-model:generateContent", exchange -> writeJson(
                exchange,
                404,
                """
                        {"error":{"code":404,"message":"Model not found","status":"NOT_FOUND"}}
                        """));

        try {
            AiProperties properties = geminiProperties(server.getAddress().getPort());
            properties.setApiKey("test-key");
            properties.setModel("unknown-model");

            HttpGeminiApiTransport transport = new HttpGeminiApiTransport(properties, objectMapper);
            GeminiTransportException ex =
                    assertThrows(GeminiTransportException.class, () -> transport.generateJson("prompt"));

            assertEquals(404, ex.getHttpStatus());
            assertTrue(ex.getMessage().contains("404"));
            assertTrue(ex.getMessage().contains("NOT_FOUND"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void rejectsUnsupportedGeminiBaseUrlHost() {
        AiProperties properties = geminiProperties(9999);
        properties.setApiKey("test-key");
        properties.setModel(MODEL);
        properties.setGeminiApiBaseUrl("https://evil.example.com");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> new HttpGeminiApiTransport(properties, objectMapper));
        assertTrue(ex.getMessage().contains("Unsupported Gemini API base URL host"));
    }

    @Test
    void rejectsSchemeLessGoogleGeminiBaseUrl() {
        assertThrows(
                IllegalStateException.class,
                () -> HttpGeminiApiTransport.validateGeminiBaseUrl("//generativelanguage.googleapis.com"));
    }

    @Test
    void rejectsHttpGoogleGeminiBaseUrl() {
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> HttpGeminiApiTransport.validateGeminiBaseUrl("http://generativelanguage.googleapis.com"));
        assertTrue(ex.getMessage().contains("HTTPS"));
    }

    private static AiProperties geminiProperties(int port) {
        AiProperties properties = new AiProperties();
        properties.setProvider("gemini");
        properties.setTimeoutMs(1000);
        properties.setGeminiApiBaseUrl("http://localhost:" + port);
        return properties;
    }

    private static HttpServer createServer(String path, com.sun.net.httpserver.HttpHandler handler) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext(path, handler);
        server.start();
        return server;
    }

    private static String successBody(String json) {
        return """
                {"candidates":[{"content":{"parts":[{"text":"%s"}]}}]}
                """.formatted(json);
    }

    private static String readBody(HttpExchange exchange) throws Exception {
        try (InputStream inputStream = exchange.getRequestBody()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void writeJson(HttpExchange exchange, int status, String body) {
        try {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(bytes);
            }
        } catch (Exception ex) {
            throw new UncheckedIOException(ex instanceof java.io.IOException io ? io : new java.io.IOException(ex));
        }
    }
}
