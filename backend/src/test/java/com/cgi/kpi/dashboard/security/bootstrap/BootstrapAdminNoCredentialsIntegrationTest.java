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
class BootstrapAdminNoCredentialsIntegrationTest {

    @DynamicPropertySource
    static void isolatedDatabase(DynamicPropertyRegistry registry) {
        IsolatedH2Database.register(registry, "bootstrap-no-creds");
        IsolatedH2Database.disableBootstrapCredentials(registry);
    }

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private WorkspaceMembershipRepository workspaceMembershipRepository;

    @Test
    void doesNotCreateUsersWhenCredentialsAreNotConfigured() {
        assertEquals(0, appUserRepository.count());
        assertEquals(0, workspaceMembershipRepository.count());
    }
}
