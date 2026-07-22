package com.cgi.kpi.dashboard.security.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.cgi.kpi.dashboard.domain.model.AppUser;
import com.cgi.kpi.dashboard.infrastructure.persistence.AppUserRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.WorkspaceMembershipRepository;
import com.cgi.kpi.dashboard.security.config.BootstrapProperties;

@SpringBootTest
@ActiveProfiles("test")
class BootstrapAdminPreSeededUserIntegrationTest {

    @DynamicPropertySource
    static void isolatedDatabaseWithoutBootstrap(DynamicPropertyRegistry registry) {
        IsolatedH2Database.register(registry, "bootstrap-preseed");
        IsolatedH2Database.disableBootstrapCredentials(registry);
    }

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private WorkspaceMembershipRepository workspaceMembershipRepository;

    @Autowired
    private BootstrapAdminService bootstrapAdminService;

    @Autowired
    private BootstrapProperties bootstrapProperties;

    @Test
    @Transactional
    void doesNotCreateAdditionalUserWhenUserAlreadyExists() {
        assertEquals(0, appUserRepository.count());

        AppUser existing = new AppUser();
        existing.setUsername("pre-seeded-user");
        existing.setPasswordHash("$2a$10$preexistinghashvalueplaceholder");
        existing.setActive(true);
        existing.setMustChangePassword(false);
        existing.setFailedLoginCount(0);
        appUserRepository.saveAndFlush(existing);

        bootstrapProperties.setAdminUsername("env-admin");
        bootstrapProperties.setAdminPassword(UUID.randomUUID().toString());

        bootstrapAdminService.bootstrapIfNeeded();

        assertEquals(1, appUserRepository.count());
        assertEquals(0, workspaceMembershipRepository.count());
        assertEquals("pre-seeded-user", appUserRepository.findAll().getFirst().getUsername());
    }
}
