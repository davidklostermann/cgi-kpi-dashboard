package com.cgi.kpi.dashboard.kpi.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Project role coverage and capacity gap summary (no personal details).
 */
public record ProjectCapacityDto(
        UUID projectId,
        Instant factsAsOf,
        String factsBadge,
        List<RoleCapacityItemDto> roles,
        CapacitySummaryDto summary) {

    public record RoleCapacityItemDto(
            UUID id,
            String roleName,
            BigDecimal requiredFte,
            BigDecimal availableFte,
            int coveragePercent) {
    }

    public record CapacitySummaryDto(
            BigDecimal missingFte,
            LocalDate nextAvailabilityDate,
            int overloadedRoles,
            int externalOptions,
            String impactHeadline,
            String impactDetail) {
    }
}
