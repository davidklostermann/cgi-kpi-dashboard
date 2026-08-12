package com.cgi.kpi.dashboard.domain.service.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.cgi.kpi.dashboard.api.error.ApiException;
import com.cgi.kpi.dashboard.domain.model.AppUser;
import com.cgi.kpi.dashboard.domain.model.WorkspaceMembership;
import com.cgi.kpi.dashboard.domain.model.WorkspaceRole;
import com.cgi.kpi.dashboard.infrastructure.persistence.AppUserRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.WorkspaceMembershipRepository;
import com.cgi.kpi.dashboard.security.user.CurrentUserService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminUserDeleteIntegrationTest {

    private static final UUID ADMIN_ID = UUID.fromString("00000000-0000-4000-a000-000000000001");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-4000-a000-000000000002");

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private WorkspaceMembershipRepository workspaceMembershipRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private CurrentUserService currentUserService;

    @DynamicPropertySource
    static void isolatedDatabase(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                () -> "jdbc:h2:mem:admin-delete-it-"
                        + UUID.randomUUID()
                        + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH");
        registry.add("app.bootstrap.admin-username", () -> "");
        registry.add("app.bootstrap.admin-password", () -> "");
    }

    @BeforeEach
    void setup() {
        // Setup current admin user
        AppUser admin = new AppUser();
        admin.setId(ADMIN_ID);
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("password"));
        admin.setActive(true);
        appUserRepository.save(admin);

        WorkspaceMembership adminMembership = new WorkspaceMembership();
        adminMembership.setUserId(ADMIN_ID);
        adminMembership.setWorkspaceId(com.cgi.kpi.dashboard.domain.model.WorkspaceIds.DEFAULT);
        adminMembership.setRole(WorkspaceRole.ADMIN);
        workspaceMembershipRepository.save(adminMembership);

        when(currentUserService.requireUserId()).thenReturn(ADMIN_ID);
        doNothingWhenRequireAdmin();
    }

    private void doNothingWhenRequireAdmin() {
        // MockBean handles this
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        AppUser user = new AppUser();
        user.setId(USER_ID);
        user.setUsername("user_to_delete");
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setActive(true);
        appUserRepository.save(user);

        WorkspaceMembership userMembership = new WorkspaceMembership();
        userMembership.setUserId(USER_ID);
        userMembership.setWorkspaceId(com.cgi.kpi.dashboard.domain.model.WorkspaceIds.DEFAULT);
        userMembership.setRole(WorkspaceRole.USER);
        workspaceMembershipRepository.save(userMembership);

        adminUserService.deleteUser(USER_ID);

        assertFalse(appUserRepository.findById(USER_ID).isPresent());
        assertFalse(workspaceMembershipRepository.findByUserId(USER_ID).isPresent());
    }

    @Test
    void shouldPreventSelfDeletion() {
        ApiException ex = assertThrows(ApiException.class, () -> adminUserService.deleteUser(ADMIN_ID));
        assertEquals("SELF_DELETION_FORBIDDEN", ex.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void shouldPreventDeletingLastAdmin() {
        AppUser otherUser = new AppUser();
        otherUser.setId(USER_ID);
        otherUser.setUsername("other_user");
        otherUser.setPasswordHash(passwordEncoder.encode("password"));
        otherUser.setActive(true);
        appUserRepository.save(otherUser);

        WorkspaceMembership userMembership = new WorkspaceMembership();
        userMembership.setUserId(USER_ID);
        userMembership.setWorkspaceId(com.cgi.kpi.dashboard.domain.model.WorkspaceIds.DEFAULT);
        userMembership.setRole(WorkspaceRole.USER);
        workspaceMembershipRepository.save(userMembership);

        // Try to delete ADMIN_ID which is the only admin
        ApiException ex = assertThrows(ApiException.class, () -> adminUserService.deleteUser(ADMIN_ID));
        // Actually the test above covers self-deletion. Let's try to delete another admin when they are the last one.
        
        // Let's make the other user an admin and the current admin a USER
        userMembership.setRole(WorkspaceRole.ADMIN);
        workspaceMembershipRepository.save(userMembership);
        
        WorkspaceMembership adminMembership = workspaceMembershipRepository.findByUserId(ADMIN_ID).get();
        adminMembership.setRole(WorkspaceRole.USER);
        workspaceMembershipRepository.save(adminMembership);
        
        // Now USER_ID is the last admin. Try to delete it.
        ApiException ex2 = assertThrows(ApiException.class, () -> adminUserService.deleteUser(USER_ID));
        assertEquals("LAST_ADMIN_PROTECTION", ex2.getCode());
    }
}
