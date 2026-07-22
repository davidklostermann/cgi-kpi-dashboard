package com.cgi.kpi.dashboard.security.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.cgi.kpi.dashboard.infrastructure.persistence.AppUserRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.WorkspaceMembershipRepository;

@SpringBootTest
@ActiveProfiles("test")
class BootstrapAdminWhitespacePasswordIntegrationTest {

    @DynamicPropertySource
    static void bootstrapCredentials(DynamicPropertyRegistry registry) {
        IsolatedH2Database.register(registry, "bootstrap-blank-password");
        registry.add("app.bootstrap.admin-username", () -> "invalid-credentials-user");
        registry.add("app.bootstrap.admin-password", () -> "   ");
    }

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private WorkspaceMembershipRepository workspaceMembershipRepository;

    @Test
    void doesNotCreateUserWhenPasswordIsWhitespaceOnly() {
        assertEquals(0, appUserRepository.count());
        assertEquals(0, workspaceMembershipRepository.count());
    }
}
