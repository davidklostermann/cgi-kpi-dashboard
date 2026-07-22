package com.cgi.kpi.dashboard.api.admin;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
class AdminUserControllerIntegrationTest {

    private static final String PASSWORD = "SecretPass1";

    @DynamicPropertySource
    static void isolatedDatabase(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                () -> "jdbc:h2:mem:admin-it-"
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
        createUser("admin-user", PASSWORD, true, false, WorkspaceRole.ADMIN);
        createUser("regular-user", PASSWORD, true, false, WorkspaceRole.USER);
    }

    @Test
    void listUsersForbiddenForRegularUser() throws Exception {
        MockHttpSession session = new MockHttpSession();
        login(session, "regular-user");

        mockMvc.perform(get("/api/admin/users").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void listUsersAllowedForAdmin() throws Exception {
        MockHttpSession session = new MockHttpSession();
        login(session, "admin-user");

        mockMvc.perform(get("/api/admin/users").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void createUserSuccessfully() throws Exception {
        MockHttpSession session = new MockHttpSession();
        login(session, "admin-user");

        mockMvc.perform(post("/api/admin/users")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"new-user\",\"password\":\"NewSecret1\",\"role\":\"USER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("new-user")))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    void lastAdminGuardPreventsSelfDeactivation() throws Exception {
        MockHttpSession session = new MockHttpSession();
        login(session, "admin-user");

        AppUser admin = appUserRepository.findByUsername("admin-user").orElseThrow();

        mockMvc.perform(put("/api/admin/users/" + admin.getId())
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false,\"role\":\"ADMIN\",\"mustChangePassword\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("LAST_ADMIN_PROTECTION")));
    }

    @Test
    void resetPasswordSuccessfully() throws Exception {
        MockHttpSession session = new MockHttpSession();
        login(session, "admin-user");

        AppUser user = appUserRepository.findByUsername("regular-user").orElseThrow();

        mockMvc.perform(put("/api/admin/users/" + user.getId() + "/password")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"BrandNewPass1\"}"))
                .andExpect(status().isOk());

        AppUser updatedUser = appUserRepository.findById(user.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("BrandNewPass1", updatedUser.getPasswordHash()));
        assertTrue(updatedUser.isMustChangePassword());
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
