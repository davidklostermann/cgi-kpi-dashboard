package com.cgi.kpi.dashboard.security.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.cgi.kpi.dashboard.api.error.ApiException;
import com.cgi.kpi.dashboard.domain.model.WorkspaceIds;

class CurrentUserServiceTest {

    private final CurrentUserService currentUserService = new CurrentUserService();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requireWorkspaceIdReturnsPrincipalWorkspace() {
        UUID userId = UUID.fromString("11111111-1111-4111-8111-111111111111");
        DashboardUserDetails principal = new DashboardUserDetails(
                userId,
                WorkspaceIds.DEFAULT,
                "admin",
                "hash",
                true,
                false,
                List.of());
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        assertEquals(WorkspaceIds.DEFAULT, currentUserService.requireWorkspaceId());
        assertEquals(userId, currentUserService.requireUserId());
    }

    @Test
    void requirePrincipalFailsWithoutDashboardUserDetails() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("plain-user", "n/a", List.of()));

        assertThrows(ApiException.class, currentUserService::requirePrincipal);
    }

    @Test
    void requireAdminAllowsAdminPrincipal() {
        DashboardUserDetails principal = new DashboardUserDetails(
                UUID.randomUUID(),
                WorkspaceIds.DEFAULT,
                "admin",
                "hash",
                true,
                false,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        currentUserService.requireAdmin();
    }

    @Test
    void requireAdminRejectsUserPrincipal() {
        DashboardUserDetails principal = new DashboardUserDetails(
                UUID.randomUUID(),
                WorkspaceIds.DEFAULT,
                "user",
                "hash",
                true,
                false,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        ApiException ex = assertThrows(ApiException.class, currentUserService::requireAdmin);
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals("FORBIDDEN", ex.getCode());
    }
}
