package com.cgi.kpi.dashboard.kpi.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * One management table row for portfolio overview (FR-2 / Story 5.3).
 */
public record PortfolioTableRowDto(
        UUID id,
        String name,
        String customerName,
        String projectLead,
        String status,
        String statusLabel,
        String currentPhaseName,
        int progressPercent,
        LocalDate plannedEndDate,
        LocalDate forecastEndDate,
        Integer scheduleDeviationDays,
        Double budgetUtilizationPercent,
        Double budgetDeviationPercent,
        Double effortDeviationPercent,
        int openRiskCount,
        int criticalIssueCount,
        Instant lastDataUpdate) {
}
