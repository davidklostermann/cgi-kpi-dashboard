package com.cgi.kpi.dashboard.api.me;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.cgi.kpi.dashboard.admin.ai.AiConfigService;
import com.cgi.kpi.dashboard.domain.model.AppUser;
import com.cgi.kpi.dashboard.infrastructure.persistence.AppUserRepository;
import com.cgi.kpi.dashboard.security.user.CurrentUserService;
import com.cgi.kpi.dashboard.security.user.WithDashboardUser;

import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithDashboardUser(role = "ADMIN")
class UserAiControllerIntegrationTest {

    private static final UUID TEST_USER_ID = UUID.fromString("e17f4b8c-5a6e-4e7d-9c0a-1b2c3d4e5f6b");
    private static final UUID USER_WITHOUT_KEY_ID = UUID.fromString("f17f4b8c-5a6e-4e7d-9c0a-1b2c3d4e5f6c");

    @DynamicPropertySource
    static void geminiProvider(DynamicPropertyRegistry registry) {
        registry.add("app.ai.provider", () -> "gemini");
        registry.add("app.ai.enabled", () -> "true");
        registry.add("app.ai.master-key", () -> "test-master-key-32-chars-long-abc");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AiConfigService aiConfigService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private CurrentUserService currentUserService;

    @BeforeEach
    void setup() {
        when(currentUserService.requireUserId()).thenReturn(TEST_USER_ID);
        when(currentUserService.requireWorkspaceId()).thenReturn(
                UUID.fromString("10000000-0000-4000-8000-000000000001"));

        AppUser user = new AppUser();
        user.setId(TEST_USER_ID);
        user.setUsername("readiness-admin");
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setActive(true);
        user.setMustChangePassword(false);
        appUserRepository.save(user);
    }

    @Test
    void readinessReturnsOkWhenUserHasActiveGeminiKey() throws Exception {
        aiConfigService.saveConfig("gemini", "gemini-2.5-flash", "valid-api-key", true);

        mockMvc.perform(get("/api/me/ai/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ready").value(true));
    }

    @Test
    void readinessReturnsAiKeyMissingWhenUserHasNoGeminiKey() throws Exception {
        when(currentUserService.requireUserId()).thenReturn(USER_WITHOUT_KEY_ID);

        mockMvc.perform(get("/api/me/ai/readiness"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AI_KEY_MISSING"))
                .andExpect(jsonPath("$.message").value(
                        "Für Ihren Benutzer ist noch kein KI-API-Key hinterlegt. Bitte hinterlegen Sie den API-Key unter KI-Einstellungen."));
    }
}
