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
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.cgi.kpi.dashboard.security.user.WithDashboardUser;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import com.cgi.kpi.dashboard.admin.ai.AiConfigService;
import com.cgi.kpi.dashboard.security.user.CurrentUserService;
import com.cgi.kpi.dashboard.security.crypto.EncryptionService;
import com.cgi.kpi.dashboard.infrastructure.persistence.AppUserRepository;
import com.cgi.kpi.dashboard.domain.model.AppUser;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.WorkspaceIds;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectRepository;
import java.time.LocalDate;
import com.cgi.kpi.dashboard.admin.ai.AiProviderConfig;
import com.cgi.kpi.dashboard.infrastructure.persistence.AiProviderConfigRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
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
    private static final UUID TEST_USER_ID = UUID.fromString("e17f4b8c-5a6e-4e7d-9c0a-1b2c3d4e5f6b");

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

    @Autowired
    private AiConfigService aiConfigService;
    @MockBean
    private CurrentUserService currentUserService;
    @MockBean
    private EncryptionService encryptionService;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void setup() {
        when(currentUserService.requireUserId()).thenReturn(TEST_USER_ID);
        when(currentUserService.requireWorkspaceId()).thenReturn(WorkspaceIds.DEFAULT);

        when(encryptionService.encrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(encryptionService.decrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        // Check if user already exists
        AppUser user = appUserRepository.findById(TEST_USER_ID).orElseGet(() -> {
            AppUser newUser = new AppUser();
            newUser.setId(TEST_USER_ID);
            newUser.setUsername("testgeminiadmin");
            newUser.setPasswordHash(passwordEncoder.encode("password"));
            newUser.setActive(true);
            newUser.setMustChangePassword(false);
            return appUserRepository.saveAndFlush(newUser); // use saveAndFlush to ensure immediate persistence
        });

        aiConfigService.saveConfig("gemini", "gemini-2.5-flash", "test-key", true);

        // Projekt für den Test anlegen
        Project project = new Project();
        project.setId(KNOWN_PROJECT_ID);
        project.setWorkspaceId(WorkspaceIds.DEFAULT);
        project.setName("Test Project for AI");
        project.setCustomerName("Test Customer");
        project.setStatus("ON_TRACK");
        project.setStartDate(LocalDate.of(2026, 1, 1));
        project.setPlannedEndDate(LocalDate.of(2026, 12, 31));
        project.setProgressPercent(50);
        projectRepository.saveAndFlush(project);
    }

    @AfterEach
    void cleanup() {
        aiConfigService.deleteConfig(TEST_USER_ID, "gemini");
        appUserRepository.deleteById(TEST_USER_ID);
        projectRepository.deleteById(KNOWN_PROJECT_ID);
    }

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
        registry.add("app.ai.api-key", () -> "env-test-key");
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
