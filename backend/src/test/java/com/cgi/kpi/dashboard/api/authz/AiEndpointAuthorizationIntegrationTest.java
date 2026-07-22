package com.cgi.kpi.dashboard.api.authz;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class AiEndpointAuthorizationIntegrationTest {

    private static final String PASSWORD = "AuthzPass1";
    private static final UUID KNOWN_PROJECT_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");

    @DynamicPropertySource
    static void isolatedDatabase(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                () -> "jdbc:h2:mem:ai-authz-"
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
        createUser("authz-user", WorkspaceRole.USER);
        createUser("authz-admin", WorkspaceRole.ADMIN);
    }

    @Test
    void workspaceUserCanReadFactEndpoints() throws Exception {
        MockHttpSession session = login("authz-user");

        mockMvc.perform(get("/api/portfolio/kpis").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeProjectCount").exists());

        mockMvc.perform(get("/api/projects/{id}", KNOWN_PROJECT_ID).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    void workspaceUserIsForbiddenOnAiEndpoints() throws Exception {
        MockHttpSession session = login("authz-user");

        mockMvc.perform(get("/api/portfolio/ai/trend-analysis").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        mockMvc.perform(get("/api/projects/{id}/ai/analysis", KNOWN_PROJECT_ID).session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        mockMvc.perform(post("/api/projects/{id}/ai/questions", KNOWN_PROJECT_ID)
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Status?\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void adminCanAccessAiEndpoints() throws Exception {
        MockHttpSession session = login("authz-admin");

        mockMvc.perform(get("/api/portfolio/ai/trend-analysis").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiGenerated").exists());

        mockMvc.perform(get("/api/projects/{id}/ai/analysis", KNOWN_PROJECT_ID).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").exists());

        mockMvc.perform(post("/api/projects/{id}/ai/questions", KNOWN_PROJECT_ID)
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Wie ist der aktuelle Fortschritt?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").exists());
    }

    @Test
    void unauthenticatedRequestsReturn401OnAiEndpoints() throws Exception {
        mockMvc.perform(get("/api/portfolio/ai/trend-analysis"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        mockMvc.perform(get("/api/projects/{id}/ai/analysis", KNOWN_PROJECT_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        mockMvc.perform(post("/api/projects/{id}/ai/questions", KNOWN_PROJECT_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Status?\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void workspaceUserIsForbiddenOnAdminApiPrefix() throws Exception {
        MockHttpSession session = login("authz-user");

        mockMvc.perform(get("/api/admin/users").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void workspaceUserCanAccessOwnPreferences() throws Exception {
        MockHttpSession session = login("authz-user");

        mockMvc.perform(get("/api/me/preferences").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferences").exists());

        mockMvc.perform(put("/api/me/preferences")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preferences\":{\"theme\":\"light\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferences.theme").value("light"));
    }

    private MockHttpSession login(String username) throws Exception {
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/auth/login")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk());
        return session;
    }

    private void createUser(String username, WorkspaceRole role) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setActive(true);
        user.setMustChangePassword(false);
        appUserRepository.saveAndFlush(user);

        WorkspaceMembership membership = new WorkspaceMembership();
        membership.setWorkspaceId(WorkspaceIds.DEFAULT);
        membership.setUserId(user.getId());
        membership.setRole(role);
        workspaceMembershipRepository.saveAndFlush(membership);
    }
}
