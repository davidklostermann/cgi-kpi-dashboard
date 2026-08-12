package com.cgi.kpi.dashboard.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDto(
        @NotBlank(message = "Current password is required") String currentPassword,
        @NotBlank(message = "New password is required")
                @Size(min = 8, max = 128, message = "New password must be at least 8 characters")
                String newPassword) {}
