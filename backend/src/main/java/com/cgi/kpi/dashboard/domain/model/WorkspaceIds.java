package com.cgi.kpi.dashboard.domain.model;

import java.util.UUID;

/**
 * Shared workspace identity constants (must match Flyway V8 default workspace seed).
 */
public final class WorkspaceIds {

    public static final UUID DEFAULT = UUID.fromString("c0000000-0000-4000-8000-000000000001");

    public static final String DEFAULT_NAME = "Default Workspace";

    private WorkspaceIds() {
    }
}
