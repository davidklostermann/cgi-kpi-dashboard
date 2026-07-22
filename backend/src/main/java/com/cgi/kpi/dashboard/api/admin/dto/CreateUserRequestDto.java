package com.cgi.kpi.dashboard.api.admin.dto;

import com.cgi.kpi.dashboard.domain.model.WorkspaceRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequestDto(
    @NotBlank @Size(max = 100) String username,
    @NotBlank @Size(min = 8, max = 100) String password,
    @NotNull WorkspaceRole role
) {}
