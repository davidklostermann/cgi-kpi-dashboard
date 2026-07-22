package com.cgi.kpi.dashboard.security.user;

import java.util.Collection;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Authenticated principal with workspace context (AD-12 / AD-13).
 */
public class DashboardUserDetails implements UserDetails {

    private final UUID userId;
    private final UUID workspaceId;
    private final String username;
    private final String passwordHash;
    private final boolean active;
    private final boolean mustChangePassword;
    private final Collection<? extends GrantedAuthority> authorities;

    public DashboardUserDetails(
            UUID userId,
            UUID workspaceId,
            String username,
            String passwordHash,
            boolean active,
            boolean mustChangePassword,
            Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.workspaceId = workspaceId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.active = active;
        this.mustChangePassword = mustChangePassword;
        this.authorities = authorities;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
