package com.cgi.kpi.dashboard.api.admin.dto;

import com.cgi.kpi.dashboard.domain.model.WorkspaceRole;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequestDto(
    @NotNull boolean active,
    @NotNull WorkspaceRole role,
    @NotNull boolean mustChangePassword
) {}
