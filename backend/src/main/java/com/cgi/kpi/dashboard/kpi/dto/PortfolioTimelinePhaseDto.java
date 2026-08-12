package com.cgi.kpi.dashboard.kpi.dto;

import java.time.LocalDate;

/**
 * Phase segment for portfolio Gantt timeline (FR-3 / Story 5.1).
 */
public record PortfolioTimelinePhaseDto(
        String name,
        String phaseType,
        LocalDate startDate,
        LocalDate endDate,
        int sortOrder) {
}
