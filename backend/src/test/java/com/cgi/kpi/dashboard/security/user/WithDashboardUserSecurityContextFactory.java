package com.cgi.kpi.dashboard.security.user;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.domain.model.AppUser;
import com.cgi.kpi.dashboard.domain.model.WorkspaceIds;
import com.cgi.kpi.dashboard.domain.model.WorkspaceMembership;
import com.cgi.kpi.dashboard.domain.model.WorkspaceRole;
import com.cgi.kpi.dashboard.infrastructure.persistence.AppUserRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.WorkspaceMembershipRepository;

@Component
public class WithDashboardUserSecurityContextFactory implements WithSecurityContextFactory<WithDashboardUser> {

    static final UUID TEST_USER_ID = UUID.fromString("d0000000-0000-4000-8000-000000000099");

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private WorkspaceMembershipRepository workspaceMembershipRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public SecurityContext createSecurityContext(WithDashboardUser annotation) {
        ensurePersistedUser(annotation.username(), annotation.role());

        DashboardUserDetails principal = new DashboardUserDetails(
                TEST_USER_ID,
                WorkspaceIds.DEFAULT,
                annotation.username(),
                "{noop}unused",
                true,
                annotation.mustChangePassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + annotation.role())));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        return context;
    }

    private void ensurePersistedUser(String username, String role) {
        AppUser user = appUserRepository.findByUsername(username).orElseGet(() -> {
            AppUser created = new AppUser();
            created.setId(TEST_USER_ID);
            created.setUsername(username);
            created.setPasswordHash(passwordEncoder.encode("TestAdminPass1"));
            created.setActive(true);
            created.setMustChangePassword(false);
            return appUserRepository.saveAndFlush(created);
        });

        if (workspaceMembershipRepository
                .findByWorkspaceIdAndUserId(WorkspaceIds.DEFAULT, user.getId())
                .isEmpty()) {
            WorkspaceMembership membership = new WorkspaceMembership();
            membership.setWorkspaceId(WorkspaceIds.DEFAULT);
            membership.setUserId(user.getId());
            membership.setRole(WorkspaceRole.valueOf(role));
            workspaceMembershipRepository.saveAndFlush(membership);
        }
    }
}
