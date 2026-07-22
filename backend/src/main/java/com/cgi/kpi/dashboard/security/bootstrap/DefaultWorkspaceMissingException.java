package com.cgi.kpi.dashboard.security.bootstrap;

import java.util.UUID;

/**
 * Thrown when bootstrap requires the default workspace but it is absent from the database.
 */
public class DefaultWorkspaceMissingException extends RuntimeException {

    public DefaultWorkspaceMissingException(UUID workspaceId) {
        super("Default workspace not found: " + workspaceId);
    }
}
