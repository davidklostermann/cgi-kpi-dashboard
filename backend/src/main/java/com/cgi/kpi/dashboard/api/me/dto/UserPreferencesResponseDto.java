package com.cgi.kpi.dashboard.api.me.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record UserPreferencesResponseDto(JsonNode preferences) {}
