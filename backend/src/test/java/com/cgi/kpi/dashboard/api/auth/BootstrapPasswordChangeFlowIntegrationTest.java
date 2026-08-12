package com.cgi.kpi.dashboard.api.auth;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.cgi.kpi.dashboard.domain.model.AppUser;
import com.cgi.kpi.dashboard.infrastructure.persistence.AppUserRepository;

import java.util.UUID;

/**
 * End-to-end bootstrap admin password change flow (Story 11.6).
 * Proves app user passwords are independent from {@code spring.datasource.password}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BootstrapPasswordChangeFlowIntegrationTest {

    /** Bootstrap initial password — must not equal datasource or new app password. */
    private static final String INITIAL_PASSWORD = "BootstrapInit1";

    /** Chosen new app password — explicitly not the datasource password. */
    private static final String NEW_APP_PASSWORD = "IndependentAppPass9";

    /** Configured datasource password — must not influence user password change. */
    private static final String DATASOURCE_PASSWORD = "TotallyDifferentDbSecret7";

    private static final String ADMIN_USERNAME = "flow-bootstrap-admin";

    @DynamicPropertySource
    static void configureFlow(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                () -> "jdbc:h2:mem:bootstrap-pwd-flow-"
                        + UUID.randomUUID()
                        + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH");
        registry.add("app.bootstrap.admin-username", () -> ADMIN_USERNAME);
        registry.add("app.bootstrap.admin-password", () -> INITIAL_PASSWORD);
        registry.add("spring.datasource.password", () -> DATASOURCE_PASSWORD);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void resetBootstrapAdminPassword() {
        AppUser user = appUserRepository.findByUsername(ADMIN_USERNAME).orElseThrow();
        user.setPasswordHash(passwordEncoder.encode(INITIAL_PASSWORD));
        user.setMustChangePassword(true);
        user.setActive(true);
        appUserRepository.saveAndFlush(user);
    }

    @Test
    void bootstrapAdminMustChangePasswordThenAcceptsIndependentNewPassword() throws Exception {
        assertNotEquals(INITIAL_PASSWORD, DATASOURCE_PASSWORD);
        assertNotEquals(NEW_APP_PASSWORD, DATASOURCE_PASSWORD);
        assertNotEquals(NEW_APP_PASSWORD, INITIAL_PASSWORD);

        AppUser bootstrapped =
                appUserRepository.findByUsername(ADMIN_USERNAME).orElseThrow();
        assertTrue(bootstrapped.isMustChangePassword());
        assertTrue(bootstrapped.getPasswordHash().startsWith("$2"));
        assertFalse(bootstrapped.getPasswordHash().contains(INITIAL_PASSWORD));

        MockHttpSession session = new MockHttpSession();
        login(session, INITIAL_PASSWORD);

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mustChangePassword", is(true)));

        changePassword(session, INITIAL_PASSWORD, NEW_APP_PASSWORD);

        AppUser updated = appUserRepository.findByUsername(ADMIN_USERNAME).orElseThrow();
        assertFalse(updated.isMustChangePassword());
        assertTrue(updated.getPasswordHash().startsWith("$2"));
        assertFalse(updated.getPasswordHash().contains(NEW_APP_PASSWORD));
        assertTrue(passwordEncoder.matches(NEW_APP_PASSWORD, updated.getPasswordHash()));
        assertFalse(passwordEncoder.matches(INITIAL_PASSWORD, updated.getPasswordHash()));

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mustChangePassword", is(false)));

        mockMvc.perform(post("/api/auth/logout").session(session).with(csrf()))
                .andExpect(status().isNoContent());

        MockHttpSession newSession = new MockHttpSession();
        login(newSession, NEW_APP_PASSWORD);

        mockMvc.perform(get("/api/auth/me").session(newSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(ADMIN_USERNAME)));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + ADMIN_USERNAME + "\",\"password\":\"" + INITIAL_PASSWORD + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("BAD_CREDENTIALS")));
    }

    @Test
    void newPasswordShorterThanEightCharactersReturnsValidationError() throws Exception {
        MockHttpSession session = new MockHttpSession();
        login(session, INITIAL_PASSWORD);

        mockMvc.perform(post("/api/auth/change-password")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"" + INITIAL_PASSWORD + "\",\"newPassword\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", is("New password must be at least 8 characters")));

        AppUser user = appUserRepository.findByUsername(ADMIN_USERNAME).orElseThrow();
        assertTrue(user.isMustChangePassword());
        assertTrue(passwordEncoder.matches(INITIAL_PASSWORD, user.getPasswordHash()));
    }

    @Test
    void newPasswordSameAsCurrentReturnsBadRequest() throws Exception {
        MockHttpSession session = new MockHttpSession();
        login(session, INITIAL_PASSWORD);

        mockMvc.perform(post("/api/auth/change-password")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"" + INITIAL_PASSWORD + "\",\"newPassword\":\""
                                + INITIAL_PASSWORD + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.message", is("New password must differ from current password")));
    }

    @Test
    void datasourcePasswordHasNoEffectOnUserPasswordChange() throws Exception {
        assertNotEquals(DATASOURCE_PASSWORD, NEW_APP_PASSWORD);

        MockHttpSession session = new MockHttpSession();
        login(session, INITIAL_PASSWORD);

        mockMvc.perform(post("/api/auth/change-password")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"" + INITIAL_PASSWORD + "\",\"newPassword\":\""
                                + DATASOURCE_PASSWORD + "\"}"))
                .andExpect(status().isNoContent());

        AppUser user = appUserRepository.findByUsername(ADMIN_USERNAME).orElseThrow();
        assertFalse(user.isMustChangePassword());
        assertTrue(passwordEncoder.matches(DATASOURCE_PASSWORD, user.getPasswordHash()));
    }

    private void login(MockHttpSession session, String password) throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + ADMIN_USERNAME + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk());
    }

    private void changePassword(MockHttpSession session, String currentPassword, String newPassword)
            throws Exception {
        mockMvc.perform(post("/api/auth/change-password")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"" + currentPassword + "\",\"newPassword\":\""
                                + newPassword + "\"}"))
                .andExpect(status().isNoContent());
    }
}
