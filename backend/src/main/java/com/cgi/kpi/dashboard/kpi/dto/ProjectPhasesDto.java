package com.cgi.kpi.dashboard.kpi.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Project phases and milestones for detail Gantt (FR-5 / Story 6.4).
 */
public record ProjectPhasesDto(
        UUID projectId,
        String projectName,
        LocalDate startDate,
        LocalDate plannedEndDate,
        LocalDate forecastEndDate,
        LocalDate actualEndDate,
        Integer scheduleDeviationDays,
        String status,
        String statusLabel,
        List<ProjectPhaseItemDto> phases,
        List<ProjectMilestoneItemDto> milestones,
        String accessibilitySummary) {

    public record ProjectPhaseItemDto(
            String name,
            String phaseType,
            String status,
            String statusLabel,
            LocalDate plannedStartDate,
            LocalDate plannedEndDate,
            LocalDate actualOrForecastStartDate,
            LocalDate actualOrForecastEndDate,
            Integer deviationDays,
            String blockers,
            int sortOrder) {
    }

    public record ProjectMilestoneItemDto(
            String name,
            String status,
            String statusLabel,
            LocalDate plannedDueDate,
            LocalDate actualOrForecastDate,
            Integer deviationDays,
            boolean overdue,
            String blockers) {
    }
}
