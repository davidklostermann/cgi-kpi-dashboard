package com.cgi.kpi.dashboard.api.auth;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

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
import org.springframework.transaction.annotation.Transactional;

import com.cgi.kpi.dashboard.domain.model.AppUser;
import com.cgi.kpi.dashboard.domain.model.WorkspaceIds;
import com.cgi.kpi.dashboard.domain.model.WorkspaceMembership;
import com.cgi.kpi.dashboard.domain.model.WorkspaceRole;
import com.cgi.kpi.dashboard.infrastructure.persistence.AppUserRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.WorkspaceMembershipRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    private static final String PASSWORD = "SecretPass1";

    @DynamicPropertySource
    static void isolatedDatabase(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                () -> "jdbc:h2:mem:auth-it-"
                        + UUID.randomUUID()
                        + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH");
        registry.add("app.bootstrap.admin-username", () -> "");
        registry.add("app.bootstrap.admin-password", () -> "");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private WorkspaceMembershipRepository workspaceMembershipRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void seedUsers() {
        createUser("active-user", PASSWORD, true, false, WorkspaceRole.USER);
        createUser("disabled-user", PASSWORD, false, false, WorkspaceRole.USER);
        createUser("must-change-user", PASSWORD, true, true, WorkspaceRole.ADMIN);
        createUser("session-user-a", PASSWORD, true, false, WorkspaceRole.USER);
        createUser("session-user-b", PASSWORD, true, false, WorkspaceRole.USER);
    }

    @Test
    void meWithoutAuthenticationReturns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("UNAUTHORIZED")));
    }

    @Test
    void loginWithValidCredentialsCreatesSession() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/api/auth/login")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"active-user\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("active-user")))
                .andExpect(jsonPath("$.mustChangePassword", is(false)))
                .andExpect(jsonPath("$.roles", containsInAnyOrder("ROLE_USER")));

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("active-user")));
    }

    @Test
    void loginWithInvalidCredentialsReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"active-user\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("BAD_CREDENTIALS")));
    }

    @Test
    void loginDisabledUserReturns403() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"disabled-user\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("ACCOUNT_DISABLED")));
    }

    @Test
    void logoutInvalidatesSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        login(session, "active-user");

        mockMvc.perform(post("/api/auth/logout").session(session).with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void mustChangePasswordBlocksProtectedApi() throws Exception {
        MockHttpSession session = new MockHttpSession();
        login(session, "must-change-user");

        mockMvc.perform(get("/api/portfolio/kpis").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("PASSWORD_CHANGE_REQUIRED")));

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mustChangePassword", is(true)));
    }

    @Test
    void mustChangePasswordBlocksActuatorInfo() throws Exception {
        MockHttpSession session = new MockHttpSession();
        login(session, "must-change-user");

        mockMvc.perform(get("/actuator/info").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("PASSWORD_CHANGE_REQUIRED")));
    }

    @Test
    void changePasswordWithWrongCurrentPasswordReturns401() throws Exception {
        MockHttpSession session = new MockHttpSession();
        login(session, "must-change-user");

        mockMvc.perform(post("/api/auth/change-password")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"wrong-password\",\"newPassword\":\"NewSecret2\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("BAD_CREDENTIALS")));
    }

    @Test
    void changePasswordClearsMustChangeFlag() throws Exception {
        MockHttpSession session = new MockHttpSession();
        login(session, "must-change-user");

        mockMvc.perform(post("/api/auth/change-password")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"" + PASSWORD + "\",\"newPassword\":\"NewSecret2\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mustChangePassword", is(false)));

        mockMvc.perform(get("/api/portfolio/kpis").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void parallelSessionsAreIndependent() throws Exception {
        MockHttpSession sessionA = new MockHttpSession();
        MockHttpSession sessionB = new MockHttpSession();

        login(sessionA, "session-user-a");
        login(sessionB, "session-user-b");

        mockMvc.perform(get("/api/auth/me").session(sessionA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("session-user-a")));

        mockMvc.perform(get("/api/auth/me").session(sessionB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("session-user-b")));
    }

    @Test
    void deactivatedUserSessionIsRejectedOnNextRequest() throws Exception {
        MockHttpSession session = new MockHttpSession();
        login(session, "active-user");

        AppUser user = appUserRepository.findByUsername("active-user").orElseThrow();
        user.setActive(false);
        appUserRepository.saveAndFlush(user);

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("UNAUTHORIZED")));
    }

    @Test
    void loginWithoutCsrfTokenIsForbidden() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"active-user\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void logoutWithoutCsrfTokenIsForbidden() throws Exception {
        MockHttpSession session = new MockHttpSession();
        login(session, "active-user");

        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void changePasswordWithoutCsrfTokenIsForbidden() throws Exception {
        MockHttpSession session = new MockHttpSession();
        login(session, "must-change-user");

        mockMvc.perform(post("/api/auth/change-password")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"" + PASSWORD + "\",\"newPassword\":\"NewSecret2\"}"))
                .andExpect(status().isForbidden());
    }

    private void login(MockHttpSession session, String username) throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk());
    }

    private void createUser(
            String username,
            String password,
            boolean active,
            boolean mustChangePassword,
            WorkspaceRole role) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setActive(active);
        user.setMustChangePassword(mustChangePassword);
        appUserRepository.saveAndFlush(user);

        WorkspaceMembership membership = new WorkspaceMembership();
        membership.setWorkspaceId(WorkspaceIds.DEFAULT);
        membership.setUserId(user.getId());
        membership.setRole(role);
        workspaceMembershipRepository.saveAndFlush(membership);
    }
}
