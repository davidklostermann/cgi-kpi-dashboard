package com.cgi.kpi.dashboard.security.user;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cgi.kpi.dashboard.api.error.ApiException;

/**
 * Resolves the authenticated {@link DashboardUserDetails} for service/reader layers (Story 12.1).
 * AuthZ IDs always come from the security context — never from client input.
 */
@Service
public class CurrentUserService {

    public DashboardUserDetails requirePrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof DashboardUserDetails details)) {
            throw new ApiException("UNAUTHORIZED", "Authentication required", HttpStatus.UNAUTHORIZED);
        }
        return details;
    }

    public UUID requireUserId() {
        return requirePrincipal().getUserId();
    }

    public UUID requireWorkspaceId() {
        return requirePrincipal().getWorkspaceId();
    }

    /** Throws 403 if the authenticated principal does not have {@code ROLE_ADMIN} (Story 12.2). */
    public void requireAdmin() {
        DashboardUserDetails principal = requirePrincipal();
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!isAdmin) {
            throw new ApiException("FORBIDDEN", "Admin role required", HttpStatus.FORBIDDEN);
        }
    }
}
