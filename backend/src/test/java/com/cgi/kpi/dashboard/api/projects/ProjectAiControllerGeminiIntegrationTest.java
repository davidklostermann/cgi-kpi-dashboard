package com.cgi.kpi.dashboard.api.projects;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.cgi.kpi.dashboard.security.user.WithDashboardUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithDashboardUser(role = "ADMIN")
class ProjectAiControllerGeminiIntegrationTest {

    private static final UUID KNOWN_PROJECT_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");

    private static final HttpServer server;

    static {
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext(
                    "/v1beta/models/gemini-2.5-flash:generateContent",
                    ProjectAiControllerGeminiIntegrationTest::handle);
            server.start();
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @DynamicPropertySource
    static void geminiProperties(DynamicPropertyRegistry registry) {
        registry.add("app.ai.provider", () -> "gemini");
        registry.add("app.ai.model", () -> "gemini-2.5-flash");
        registry.add("app.ai.api-key", () -> "test-key");
        registry.add("app.ai.gemini-api-base-url", () -> "http://localhost:" + server.getAddress().getPort());
    }

    @Test
    void gemini403ReturnsProviderError() throws Exception {
        currentStatus = 403;
        currentBody =
                """
                        {"error":{"code":403,"message":"API key not valid","status":"PERMISSION_DENIED"}}
                        """;

        mockMvc.perform(get("/api/projects/{id}/ai/analysis", KNOWN_PROJECT_ID).queryParam("refresh", "true"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code", is("AI_PROVIDER_ERROR")))
                .andExpect(jsonPath("$.message", is("Gemini-Authentifizierung fehlgeschlagen. API-Key und Berechtigungen prüfen.")));
    }

    @Test
    void gemini404ReturnsProviderError() throws Exception {
        currentStatus = 404;
        currentBody =
                """
                        {"error":{"code":404,"message":"Model not found","status":"NOT_FOUND"}}
                        """;

        mockMvc.perform(get("/api/projects/{id}/ai/analysis", KNOWN_PROJECT_ID).queryParam("refresh", "true"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code", is("AI_PROVIDER_ERROR")))
                .andExpect(jsonPath("$.message", is("Gemini-Modell nicht gefunden. APP_AI_MODEL prüfen.")));
    }

    @Test
    void geminiSuccessReturnsAnalysis() throws Exception {
        currentStatus = 200;
        currentBody =
                """
                        {"candidates":[{"content":{"parts":[{"text":"{\\"summary\\":\\"Gemini OK\\",\\"priorities\\":[{\\"rank\\":1,\\"title\\":\\"Test\\",\\"reason\\":\\"OK\\",\\"evidenceFactIds\\":[\\"kpi.progressPercent\\"]}],\\"suggestedActions\\":[],\\"missingData\\":[]}"}]}}]}
                        """;

        mockMvc.perform(get("/api/projects/{id}/ai/analysis", KNOWN_PROJECT_ID).queryParam("refresh", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary", is("Gemini OK")))
                .andExpect(jsonPath("$.aiGenerated", is(true)));
    }

    private static volatile int currentStatus = 200;
    private static volatile String currentBody = "{}";

    private static void handle(HttpExchange exchange) {
        if (!"test-key".equals(exchange.getRequestHeaders().getFirst("x-goog-api-key"))) {
            writeJson(exchange, 403, """
                    {"error":{"code":403,"message":"API key not valid","status":"PERMISSION_DENIED"}}
                    """);
            return;
        }
        writeJson(exchange, currentStatus, currentBody);
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
