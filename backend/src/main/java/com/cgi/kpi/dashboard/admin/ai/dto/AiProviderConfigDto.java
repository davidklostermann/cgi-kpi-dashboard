package com.cgi.kpi.dashboard.admin.ai.dto;

import java.util.UUID;

/**
 * DTO for AI provider configuration. The API key is always masked for the UI.
 */
public record AiProviderConfigDto(
        UUID id,
        String provider,
        String model,
        String apiKeyMasked,
        boolean enabled,
        UUID userId
) {
    public static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return "";
        }
        if (apiKey.length() <= 8) {
            return "****";
        }
        return "********" + apiKey.substring(apiKey.length() - 4);
    }
}
