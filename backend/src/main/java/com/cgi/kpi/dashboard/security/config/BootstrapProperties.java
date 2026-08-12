package com.cgi.kpi.dashboard.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.bootstrap")
public class BootstrapProperties {

    private String adminUsername = "";

    private String adminPassword = "";

    /** When true, bootstrap admin must change password on first login. */
    private boolean mustChangePassword = true;

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public boolean hasCredentials() {
        return trimmedUsername() != null && adminPassword != null && !adminPassword.isBlank();
    }

    /**
     * Returns trimmed username or {@code null} when blank after trim.
     */
    public String trimmedUsername() {
        if (adminUsername == null) {
            return null;
        }
        String trimmed = adminUsername.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
