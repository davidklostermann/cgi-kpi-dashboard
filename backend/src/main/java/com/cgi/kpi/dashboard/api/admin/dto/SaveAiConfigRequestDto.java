package com.cgi.kpi.dashboard.api.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating AI provider configuration.
 */
public record SaveAiConfigRequestDto(
        @NotBlank @Size(max = 50) String provider,
        @NotBlank @Size(max = 100) String model,
        String apiKey, // Optional: only update if not blank
        boolean enabled
) {
}
