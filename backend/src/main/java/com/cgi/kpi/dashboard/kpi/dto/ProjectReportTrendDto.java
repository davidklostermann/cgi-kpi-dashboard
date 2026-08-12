package com.cgi.kpi.dashboard.kpi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Two-report-stand comparison for portfolio pattern detection (deterministic).
 */
public record ProjectReportTrendDto(
        UUID projectId,
        String projectName,
        LocalDate previousDate,
        LocalDate currentDate,
        Integer previousScheduleDeviationDays,
        Integer currentScheduleDeviationDays,
        String previousStatus,
        String currentStatus,
        int previousOpenRiskCount,
        int currentOpenRiskCount,
        BigDecimal previousActualBudget,
        BigDecimal currentActualBudget,
        int previousProgressPercent,
        int currentProgressPercent) {
}
