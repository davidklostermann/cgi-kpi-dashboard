package com.cgi.kpi.dashboard.security.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.cgi.kpi.dashboard.domain.model.AppUser;
import com.cgi.kpi.dashboard.infrastructure.persistence.AppUserRepository;

/**
 * Guards the public Docker demo credentials documented in README / .env.example.
 * Username {@code admin1} and password {@code DemoAdmin1!} must authenticate after bootstrap,
 * with {@code mustChangePassword=false} as in the docker profile.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DockerDemoAdminLoginIntegrationTest {

    static final String DEMO_USERNAME = "admin1";
    static final String DEMO_PASSWORD = "DemoAdmin1!";

    @DynamicPropertySource
    static void dockerDemoBootstrap(DynamicPropertyRegistry registry) {
        IsolatedH2Database.register(registry, "docker-demo-admin-login");
        registry.add("app.bootstrap.admin-username", () -> DEMO_USERNAME);
        registry.add("app.bootstrap.admin-password", () -> DEMO_PASSWORD);
        registry.add("app.bootstrap.must-change-password", () -> "false");
    }

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void bootstrapsAdmin1WithBcryptHashMatchingDemoPassword() {
        assertEquals(1, appUserRepository.count());

        AppUser admin = appUserRepository.findByUsername(DEMO_USERNAME).orElseThrow();
        assertTrue(admin.isActive());
        assertFalse(admin.isMustChangePassword());
        assertTrue(admin.getPasswordHash().startsWith("$2"));
        assertTrue(passwordEncoder.matches(DEMO_PASSWORD, admin.getPasswordHash()));
    }

    @Test
    void loginSucceedsWithDocumentedDockerDemoCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + DEMO_USERNAME + "\",\"password\":\"" + DEMO_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(DEMO_USERNAME))
                .andExpect(jsonPath("$.mustChangePassword").value(false));
    }

    @Test
    void loginFailsForWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + DEMO_USERNAME + "\",\"password\":\"WrongPass1!\"}"))
                .andExpect(status().isUnauthorized());
    }
}
