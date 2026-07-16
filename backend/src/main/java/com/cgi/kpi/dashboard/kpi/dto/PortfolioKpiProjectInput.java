package com.cgi.kpi.dashboard.kpi.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

/**
 * Minimale Projektdaten für deterministische Portfolio-KPI-Berechnung in {@code kpi.*}.
 */
public record PortfolioKpiProjectInput(
        UUID projectId,
        String status,
        String customerName,
        String projectLead,
        String currentPhaseName,
        YearMonth dataReportMonth,
        int progressPercent,
        Integer scheduleDeviationDays,
        BigDecimal plannedBudget,
        BigDecimal actualBudget) {
}
