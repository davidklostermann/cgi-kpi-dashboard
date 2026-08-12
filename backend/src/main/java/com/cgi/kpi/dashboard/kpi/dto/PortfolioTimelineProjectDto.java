package com.cgi.kpi.dashboard.kpi.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * One project row for portfolio Gantt timeline (FR-3 / Story 5.1).
 */
public record PortfolioTimelineProjectDto(
        UUID id,
        String name,
        LocalDate startDate,
        LocalDate plannedEndDate,
        LocalDate forecastEndDate,
        LocalDate actualEndDate,
        Integer scheduleDeviationDays,
        String status,
        String statusLabel,
        List<PortfolioTimelinePhaseDto> phases,
        List<PortfolioTimelineMilestoneDto> milestones) {
}
