package com.cgi.kpi.dashboard.api.auth.dto;

import java.util.List;
import java.util.UUID;

public record AuthMeResponseDto(
        UUID userId,
        UUID workspaceId,
        String username,
        List<String> roles,
        boolean mustChangePassword) {}
