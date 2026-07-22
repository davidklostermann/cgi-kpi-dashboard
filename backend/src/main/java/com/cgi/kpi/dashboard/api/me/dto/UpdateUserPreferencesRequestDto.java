package com.cgi.kpi.dashboard.api.me.dto;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotNull;

public record UpdateUserPreferencesRequestDto(@NotNull JsonNode preferences) {}
