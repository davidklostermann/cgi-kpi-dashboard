package com.cgi.kpi.dashboard.security.user;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.cgi.kpi.dashboard.domain.model.AppUser;
import com.cgi.kpi.dashboard.domain.model.WorkspaceIds;
import com.cgi.kpi.dashboard.domain.model.WorkspaceMembership;
import com.cgi.kpi.dashboard.domain.model.WorkspaceRole;
import com.cgi.kpi.dashboard.infrastructure.persistence.AppUserRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.WorkspaceMembershipRepository;

@SpringBootTest
@ActiveProfiles("test")
class DashboardUserDetailsServiceTest {

    @Autowired
    private DashboardUserDetailsService dashboardUserDetailsService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private WorkspaceMembershipRepository workspaceMembershipRepository;

    @Test
    @Transactional
    void loadsActiveUserWithWorkspaceAndRole() {
        AppUser user = new AppUser();
        user.setUsername("details-service-user");
        user.setPasswordHash("$2a$10$placeholderhashvalue000000000000000000000000000000000");
        user.setActive(true);
        appUserRepository.saveAndFlush(user);

        WorkspaceMembership membership = new WorkspaceMembership();
        membership.setWorkspaceId(WorkspaceIds.DEFAULT);
        membership.setUserId(user.getId());
        membership.setRole(WorkspaceRole.ADMIN);
        workspaceMembershipRepository.saveAndFlush(membership);

        UserDetails loaded = dashboardUserDetailsService.loadUserByUsername("details-service-user");

        assertInstanceOf(DashboardUserDetails.class, loaded);
        DashboardUserDetails details = (DashboardUserDetails) loaded;
        assertEquals(user.getId(), details.getUserId());
        assertEquals(WorkspaceIds.DEFAULT, details.getWorkspaceId());
        assertTrue(loaded.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())));
    }

    @Test
    @Transactional
    void rejectsDisabledUser() {
        AppUser user = new AppUser();
        user.setUsername("disabled-details-user");
        user.setPasswordHash("$2a$10$placeholderhashvalue000000000000000000000000000000000");
        user.setActive(false);
        appUserRepository.saveAndFlush(user);

        WorkspaceMembership membership = new WorkspaceMembership();
        membership.setWorkspaceId(WorkspaceIds.DEFAULT);
        membership.setUserId(user.getId());
        membership.setRole(WorkspaceRole.USER);
        workspaceMembershipRepository.saveAndFlush(membership);

        assertThrows(DisabledException.class, () -> dashboardUserDetailsService.loadUserByUsername("disabled-details-user"));
    }

    @Test
    @Transactional
    void rejectsUserWithoutDefaultWorkspaceMembership() {
        AppUser user = new AppUser();
        user.setUsername("no-membership-user");
        user.setPasswordHash("$2a$10$placeholderhashvalue000000000000000000000000000000000");
        user.setActive(true);
        appUserRepository.saveAndFlush(user);

        assertThrows(UsernameNotFoundException.class, () -> dashboardUserDetailsService.loadUserByUsername("no-membership-user"));
    }

    @Test
    void unknownUsernameThrowsNotFound() {
        assertThrows(UsernameNotFoundException.class, () -> dashboardUserDetailsService.loadUserByUsername("no-such-user"));
    }
}
