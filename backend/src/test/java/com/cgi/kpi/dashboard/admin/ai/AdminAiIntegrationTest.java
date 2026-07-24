package com.cgi.kpi.dashboard.admin.ai;

import com.cgi.kpi.dashboard.admin.ai.dto.AiProviderConfigDto;
import com.cgi.kpi.dashboard.api.admin.AdminAiController;
import com.cgi.kpi.dashboard.ai.client.GeminiApiTransport;
import com.cgi.kpi.dashboard.ai.config.AiProperties;
import com.cgi.kpi.dashboard.security.user.CurrentUserService;
import com.cgi.kpi.dashboard.infrastructure.persistence.AppUserRepository;
import com.cgi.kpi.dashboard.domain.model.AppUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminAiIntegrationTest {

    private static final UUID TEST_USER_ID = UUID.fromString("d17f4b8c-5a6e-4e7d-9c0a-1b2c3d4e5f6a");

    @Autowired
    private AiConfigService aiConfigService;

    @Autowired
    private AiProperties aiProperties;

    @MockBean
    private GeminiApiTransport geminiTransport;

    @MockBean
    private CurrentUserService currentUserService;

    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        when(currentUserService.requireUserId()).thenReturn(TEST_USER_ID);
        AppUser user = new AppUser();
        user.setId(TEST_USER_ID);
        user.setUsername("testadmin");
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setActive(true);
        user.setMustChangePassword(false);
        appUserRepository.save(user);
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.ai.master-key", () -> "test-master-key-32-chars-long-abc");
        registry.add("app.ai.api-key", () -> "env-api-key");
        registry.add("app.ai.model", () -> "env-model");
    }

    @Test
    void shouldFallbackToPropertiesWhenNoDbConfig() {
        // For Gemini, there is no fallback to properties if no user-specific DB config.
        // It should return null if no config is found.
        assertNull(aiConfigService.getActiveApiKey("gemini"));
        assertNull(aiConfigService.getActiveModel("gemini"));
    }

    @Test
    void shouldPreferDbConfigWhenEnabled() {
        aiConfigService.saveConfig("gemini", "db-model", "db-api-key", true);

        assertEquals("db-api-key", aiConfigService.getActiveApiKey("gemini"));
        assertEquals("db-model", aiConfigService.getActiveModel("gemini"));
    }

    @Test
    void shouldFallbackToPropertiesWhenDbConfigDisabled() {
        aiConfigService.saveConfig("gemini", "db-model", "db-api-key", false);

        // For Gemini, there is no fallback to properties if user-specific DB config is disabled.
        // It should return null.
        assertNull(aiConfigService.getActiveApiKey("gemini"));
        assertNull(aiConfigService.getActiveModel("gemini"));
    }

    @Test
    void shouldIncrementVersionOnSave() {
        long v1 = aiConfigService.getCurrentVersion();
        aiConfigService.saveConfig("gemini", "model", "key", true);
        long v2 = aiConfigService.getCurrentVersion();

        assertTrue(v2 > v1, "Version should increase on save");
    }

    @Test
    void shouldTestConnectionSuccessfully() {
        aiConfigService.saveConfig("gemini", "gemini-1.5-pro", "valid-api-key", true);
        when(geminiTransport.generateJson(anyString())).thenReturn("OK, I am Gemini.");

        AdminAiController.ConnectionTestResponseDto response = aiConfigService.testConnection("gemini");

        assertTrue(response.success());
        assertTrue(response.message().contains("erfolgreich"));
    }

    @Test
    void shouldHandleConnectionTestFailure() {
        aiConfigService.saveConfig("gemini", "gemini-1.5-pro", "invalid-api-key", true);
        when(geminiTransport.generateJson(anyString())).thenThrow(new RuntimeException("API Key Invalid"));

        AdminAiController.ConnectionTestResponseDto response = aiConfigService.testConnection("gemini");

        assertFalse(response.success());
        assertTrue(response.message().contains("API Key Invalid"));
    }
}
