package com.cgi.kpi.dashboard.security.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cgi.kpi.dashboard.domain.model.AppUser;
import com.cgi.kpi.dashboard.domain.model.WorkspaceIds;
import com.cgi.kpi.dashboard.domain.model.WorkspaceMembership;
import com.cgi.kpi.dashboard.domain.model.WorkspaceRole;
import com.cgi.kpi.dashboard.infrastructure.persistence.AppUserRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.WorkspaceMembershipRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.WorkspaceRepository;
import com.cgi.kpi.dashboard.security.config.BootstrapProperties;

@Service
public class BootstrapAdminService {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdminService.class);

    private final AppUserRepository appUserRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMembershipRepository workspaceMembershipRepository;
    private final BootstrapProperties bootstrapProperties;
    private final PasswordEncoder passwordEncoder;

    public BootstrapAdminService(
            AppUserRepository appUserRepository,
            WorkspaceRepository workspaceRepository,
            WorkspaceMembershipRepository workspaceMembershipRepository,
            BootstrapProperties bootstrapProperties,
            PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.workspaceRepository = workspaceRepository;
        this.workspaceMembershipRepository = workspaceMembershipRepository;
        this.bootstrapProperties = bootstrapProperties;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void bootstrapIfNeeded() {
        if (appUserRepository.count() > 0) {
            log.info("Bootstrap skipped: users already exist");
            return;
        }

        if (!bootstrapProperties.hasCredentials()) {
            log.warn("Bootstrap skipped: credentials not configured");
            return;
        }

        workspaceRepository
                .findById(WorkspaceIds.DEFAULT)
                .orElseThrow(() -> new DefaultWorkspaceMissingException(WorkspaceIds.DEFAULT));

        String username = bootstrapProperties.trimmedUsername();
        if (username == null || username.length() > 100) {
            log.warn("Bootstrap skipped: invalid username");
            return;
        }

        String rawPassword = bootstrapProperties.getAdminPassword();

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setActive(true);
        user.setMustChangePassword(true);
        user.setFailedLoginCount(0);
        appUserRepository.saveAndFlush(user);

        WorkspaceMembership membership = new WorkspaceMembership();
        membership.setWorkspaceId(WorkspaceIds.DEFAULT);
        membership.setUserId(user.getId());
        membership.setRole(WorkspaceRole.ADMIN);
        workspaceMembershipRepository.saveAndFlush(membership);

        log.info("Bootstrap admin created");
    }
}
