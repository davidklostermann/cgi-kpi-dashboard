package com.cgi.kpi.dashboard.security.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.cgi.kpi.dashboard.domain.model.AppUser;
import com.cgi.kpi.dashboard.domain.model.WorkspaceIds;
import com.cgi.kpi.dashboard.domain.model.WorkspaceRole;
import com.cgi.kpi.dashboard.infrastructure.persistence.AppUserRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.WorkspaceMembershipRepository;

@SpringBootTest
@ActiveProfiles("test")
class BootstrapAdminIntegrationTest {

    private static final String TEST_USERNAME = "  integration-bootstrap-admin  ";
    private static final String TEST_PASSWORD = UUID.randomUUID().toString();

    @DynamicPropertySource
    static void bootstrapCredentials(DynamicPropertyRegistry registry) {
        IsolatedH2Database.register(registry, "bootstrap-admin");
        registry.add("app.bootstrap.admin-username", () -> TEST_USERNAME);
        registry.add("app.bootstrap.admin-password", () -> TEST_PASSWORD);
    }

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private WorkspaceMembershipRepository workspaceMembershipRepository;

    @Autowired
    private BootstrapAdminService bootstrapAdminService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createsSingleAdminWithMembershipOnStartup() {
        assertEquals(1, appUserRepository.count());
        assertEquals(1, workspaceMembershipRepository.count());

        AppUser admin = appUserRepository.findByUsername("integration-bootstrap-admin").orElseThrow();
        assertTrue(admin.isActive());
        assertTrue(admin.isMustChangePassword());
        assertEquals(0, admin.getFailedLoginCount());
        assertNotEquals(TEST_PASSWORD, admin.getPasswordHash());
        assertTrue(admin.getPasswordHash().startsWith("$2"));
        assertTrue(passwordEncoder.matches(TEST_PASSWORD, admin.getPasswordHash()));

        assertEquals(
                WorkspaceRole.ADMIN,
                workspaceMembershipRepository
                        .findByWorkspaceIdAndUserId(WorkspaceIds.DEFAULT, admin.getId())
                        .orElseThrow()
                        .getRole());
    }

    @Test
    void bootstrapIsIdempotentWhenUsersAlreadyExist() {
        long usersBefore = appUserRepository.count();
        long membershipsBefore = workspaceMembershipRepository.count();

        bootstrapAdminService.bootstrapIfNeeded();

        assertEquals(usersBefore, appUserRepository.count());
        assertEquals(membershipsBefore, workspaceMembershipRepository.count());
    }
}
