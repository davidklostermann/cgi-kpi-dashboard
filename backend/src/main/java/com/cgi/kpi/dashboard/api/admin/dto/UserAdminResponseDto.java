package com.cgi.kpi.dashboard.api.admin.dto;

import java.time.Instant;
import java.util.UUID;
import com.cgi.kpi.dashboard.domain.model.WorkspaceRole;

public record UserAdminResponseDto(
    UUID id,
    String username,
    boolean active,
    WorkspaceRole role,
    boolean mustChangePassword,
    Instant createdAt,
    Instant updatedAt
) {}
