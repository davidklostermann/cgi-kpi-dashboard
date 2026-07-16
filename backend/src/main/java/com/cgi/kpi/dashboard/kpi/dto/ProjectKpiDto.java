package com.cgi.kpi.dashboard.kpi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Deterministic project management KPIs (FR-5 / Story 6.1). Plan/Ist/Prognose separated.
 */
public record ProjectKpiDto(
        UUID projectId,
        String status,
        String statusLabel,
        int progressPercent,
        String currentPhaseName,
        ProjectScheduleKpiDto schedule,
        ProjectBudgetKpiDto budget,
        ProjectEffortKpiDto effort,
        ProjectRiskProblemKpiDto risks,
        ProjectRiskProblemKpiDto problems) {

    public record ProjectScheduleKpiDto(
            Double timeElapsedPercent,
            Integer deviationDays,
            LocalDate plannedEndDate,
            LocalDate forecastEndDate,
            LocalDate actualEndDate) {
    }

    public record ProjectBudgetKpiDto(
            BigDecimal planned,
            BigDecimal actual,
            Double utilizationPercent,
            Double deviationPercent,
            BigDecimal remaining,
            BigDecimal forecastAtCompletion) {
    }

    public record ProjectEffortKpiDto(
            BigDecimal plannedDays,
            BigDecimal actualDays,
            Double deviationPercent,
            BigDecimal remainingDays,
            BigDecimal forecastAtCompletionDays) {
    }

    public record ProjectRiskProblemKpiDto(
            int openCount,
            int criticalOpenCount) {
    }
}
