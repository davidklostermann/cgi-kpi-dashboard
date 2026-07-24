package com.cgi.kpi.dashboard.admin.ai;

import com.cgi.kpi.dashboard.admin.ai.dto.AiProviderConfigDto;
import com.cgi.kpi.dashboard.api.admin.AdminAiController;
import com.cgi.kpi.dashboard.ai.client.GeminiApiTransport;
import com.cgi.kpi.dashboard.ai.config.AiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminAiIntegrationTest {

    @Autowired
    private AiConfigService aiConfigService;

    @Autowired
    private AiProperties aiProperties;

    @MockBean
    private GeminiApiTransport geminiTransport;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.ai.master-key", () -> "test-master-key-32-chars-long-abc");
        registry.add("app.ai.api-key", () -> "env-api-key");
        registry.add("app.ai.model", () -> "env-model");
    }

    @Test
    void shouldFallbackToPropertiesWhenNoDbConfig() {
        assertEquals("env-api-key", aiConfigService.getActiveApiKey("gemini"));
        assertEquals("env-model", aiConfigService.getActiveModel("gemini"));
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

        assertEquals("env-api-key", aiConfigService.getActiveApiKey("gemini"));
        assertEquals("env-model", aiConfigService.getActiveModel("gemini"));
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
        when(geminiTransport.generateJson(anyString())).thenReturn("OK, I am Gemini.");

        AdminAiController.ConnectionTestResponseDto response = aiConfigService.testConnection("gemini");

        assertTrue(response.success());
        assertTrue(response.message().contains("erfolgreich"));
    }

    @Test
    void shouldHandleConnectionTestFailure() {
        when(geminiTransport.generateJson(anyString())).thenThrow(new RuntimeException("API Key Invalid"));

        AdminAiController.ConnectionTestResponseDto response = aiConfigService.testConnection("gemini");

        assertFalse(response.success());
        assertTrue(response.message().contains("API Key Invalid"));
    }
}
