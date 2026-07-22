package com.cgi.kpi.dashboard.api.security;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.cgi.kpi.dashboard.ai.cache.ProjectAiAnalysisCache;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto;
import com.cgi.kpi.dashboard.domain.model.AppUser;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.Workspace;
import com.cgi.kpi.dashboard.domain.model.WorkspaceIds;
import com.cgi.kpi.dashboard.domain.model.WorkspaceMembership;
import com.cgi.kpi.dashboard.domain.model.WorkspaceRole;
import com.cgi.kpi.dashboard.infrastructure.persistence.AppUserRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.WorkspaceMembershipRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.WorkspaceRepository;
import com.jayway.jsonpath.JsonPath;

/**
 * Epic 12 DoD: consolidated foreign-access and isolation integration tests (Story 12.4).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityIsolationIntegrationTest {

    private static final String PASSWORD = "SecIsoPass1";
    private static final UUID KNOWN_PROJECT_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");
    private static final UUID FOREIGN_WORKSPACE_ID = UUID.fromString("c0000000-0000-4000-8000-000000000099");
    private static final String FOREIGN_PROJECT_NAME = "Foreign-Workspace-Only-Project";
    private static final String POISON_SUMMARY = "FOREIGN-WORKSPACE-CACHE-LEAK";

    @DynamicPropertySource
    static void isolatedDatabase(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                () -> "jdbc:h2:mem:sec-isolation-"
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
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceMembershipRepository workspaceMembershipRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProjectAiAnalysisCache projectAiAnalysisCache;

    private UUID foreignProjectId;
    private AppUser userA;
    private AppUser userB;

    @BeforeEach
    void seed() {
        projectAiAnalysisCache.invalidateAll();

        Workspace foreign = new Workspace();
        foreign.setId(FOREIGN_WORKSPACE_ID);
        foreign.setName("Foreign Workspace");
        foreign.setCreatedAt(Instant.now());
        foreign.setUpdatedAt(Instant.now());
        workspaceRepository.saveAndFlush(foreign);

        Project foreignProject = new Project();
        foreignProject.setWorkspaceId(FOREIGN_WORKSPACE_ID);
        foreignProject.setName(FOREIGN_PROJECT_NAME);
        foreignProject.setCustomerName("Foreign Customer");
        foreignProject.setStatus("ON_TRACK");
        foreignProject.setStartDate(LocalDate.of(2026, 1, 1));
        foreignProject.setPlannedEndDate(LocalDate.of(2026, 12, 31));
        foreignProject.setProgressPercent(10);
        foreignProjectId = projectRepository.saveAndFlush(foreignProject).getId();

        createUser("sec-user", WorkspaceIds.DEFAULT, WorkspaceRole.USER);
        createUser("sec-admin", WorkspaceIds.DEFAULT, WorkspaceRole.ADMIN);
        userA = createUser("sec-user-a", WorkspaceIds.DEFAULT, WorkspaceRole.USER);
        userB = createUser("sec-user-b", WorkspaceIds.DEFAULT, WorkspaceRole.USER);
    }

    @Test
    void workspaceUserIsForbiddenOnAiAndAdminEndpoints() throws Exception {
        MockHttpSession session = login("sec-user");

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

        mockMvc.perform(get("/api/admin/users").session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void adminCanAccessAiEndpointsInOwnWorkspace() throws Exception {
        MockHttpSession session = login("sec-admin");

        mockMvc.perform(get("/api/portfolio/ai/trend-analysis").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiGenerated").exists());

        mockMvc.perform(get("/api/projects/{id}/ai/analysis", KNOWN_PROJECT_ID).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").exists());
    }

    @Test
    void factEndpointsRejectForeignWorkspaceProjects() throws Exception {
        MockHttpSession session = login("sec-user");

        mockMvc.perform(get("/api/portfolio/projects").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projects[*].name", not(hasItem(FOREIGN_PROJECT_NAME))));

        mockMvc.perform(get("/api/portfolio/kpis").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeProjectCount").value(19));

        mockMvc.perform(get("/api/projects/{id}", foreignProjectId).session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));

        mockMvc.perform(get("/api/projects/{id}/kpis", foreignProjectId).session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void userGetsForbiddenOnForeignProjectAiBeforeScopeCheck() throws Exception {
        MockHttpSession session = login("sec-user");

        mockMvc.perform(get("/api/projects/{id}/ai/analysis", foreignProjectId).session(session))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void adminGetsNotFoundOnForeignProjectAi() throws Exception {
        MockHttpSession session = login("sec-admin");

        mockMvc.perform(get("/api/projects/{id}/ai/analysis", foreignProjectId).session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void preferencesAreIsolatedPerUserAndIgnoreClientIdentityFields() throws Exception {
        MockHttpSession sessionA = login("sec-user-a");
        MockHttpSession sessionB = login("sec-user-b");

        mockMvc.perform(put("/api/me/preferences")
                        .session(sessionA)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preferences\":{\"theme\":\"dark\",\"userId\":\""
                                + userB.getId()
                                + "\",\"workspaceId\":\""
                                + FOREIGN_WORKSPACE_ID
                                + "\",\"settings\":{\"userId\":\""
                                + userB.getId()
                                + "\",\"workspaceId\":\""
                                + FOREIGN_WORKSPACE_ID
                                + "\"}}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferences.theme").value("dark"))
                .andExpect(jsonPath("$.preferences.userId").doesNotExist())
                .andExpect(jsonPath("$.preferences.workspaceId").doesNotExist())
                .andExpect(jsonPath("$.preferences.settings.userId").doesNotExist())
                .andExpect(jsonPath("$.preferences.settings.workspaceId").doesNotExist());

        mockMvc.perform(put("/api/me/preferences")
                        .session(sessionB)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preferences\":{\"theme\":\"light\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferences.theme").value("light"));

        mockMvc.perform(get("/api/me/preferences").session(sessionA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferences.theme").value("dark"));

        mockMvc.perform(get("/api/me/preferences").session(sessionB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferences.theme").value("light"));
    }

    @Test
    void aiAnalysisCacheHitsWithinSameWorkspace() throws Exception {
        MockHttpSession session = login("sec-admin");

        MvcResult first = mockMvc.perform(get("/api/projects/{id}/ai/analysis", KNOWN_PROJECT_ID).session(session))
                .andExpect(status().isOk())
                .andReturn();
        MvcResult second = mockMvc.perform(get("/api/projects/{id}/ai/analysis", KNOWN_PROJECT_ID).session(session))
                .andExpect(status().isOk())
                .andReturn();

        String firstGeneratedAt = JsonPath.read(first.getResponse().getContentAsString(), "$.generatedAt");
        String secondGeneratedAt = JsonPath.read(second.getResponse().getContentAsString(), "$.generatedAt");
        assertEquals(firstGeneratedAt, secondGeneratedAt);
    }

    @Test
    void aiAnalysisCacheDoesNotLeakForeignWorkspaceEntry() throws Exception {
        MockHttpSession session = login("sec-admin");

        MvcResult first = mockMvc.perform(get("/api/projects/{id}/ai/analysis", KNOWN_PROJECT_ID).session(session))
                .andExpect(status().isOk())
                .andReturn();

        String factsAsOfText = JsonPath.read(first.getResponse().getContentAsString(), "$.factsAsOf");
        Instant factsAsOf = Instant.parse(factsAsOfText);
        String foreignKey = ProjectAiAnalysisCache.buildKey(
                FOREIGN_WORKSPACE_ID, KNOWN_PROJECT_ID, factsAsOf, 0L);
        projectAiAnalysisCache.put(foreignKey, poisonedResponse(factsAsOf));

        MvcResult afterPoison = mockMvc.perform(
                        get("/api/projects/{id}/ai/analysis", KNOWN_PROJECT_ID).session(session))
                .andExpect(status().isOk())
                .andReturn();

        String summary = JsonPath.read(afterPoison.getResponse().getContentAsString(), "$.summary");
        assertNotEquals(POISON_SUMMARY, summary);

        String defaultKey = ProjectAiAnalysisCache.buildKey(
                WorkspaceIds.DEFAULT, KNOWN_PROJECT_ID, factsAsOf, 0L);
        assertEquals(
                first.getResponse().getContentAsString(),
                afterPoison.getResponse().getContentAsString());
        assertEquals(
                JsonPath.read(first.getResponse().getContentAsString(), "$.summary"),
                projectAiAnalysisCache.get(defaultKey).summary());
    }

    private ProjectAiAnalysisResponseDto poisonedResponse(Instant factsAsOf) {
        return new ProjectAiAnalysisResponseDto(
                KNOWN_PROJECT_ID,
                factsAsOf,
                Instant.parse("2099-01-01T00:00:00Z"),
                "SUCCESS",
                List.of(),
                POISON_SUMMARY,
                List.of(),
                List.of(),
                List.of(),
                true,
                "Poison");
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

    private AppUser createUser(String username, UUID workspaceId, WorkspaceRole role) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setActive(true);
        user.setMustChangePassword(false);
        appUserRepository.saveAndFlush(user);

        WorkspaceMembership membership = new WorkspaceMembership();
        membership.setWorkspaceId(workspaceId);
        membership.setUserId(user.getId());
        membership.setRole(role);
        workspaceMembershipRepository.saveAndFlush(membership);
        return user;
    }
}
