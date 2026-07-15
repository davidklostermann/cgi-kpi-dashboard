package com.cgi.kpi.dashboard.kpi.dto;

/**
 * Aggregated portfolio KPI values for API and AI reader surfaces (FR-1).
 */
public record PortfolioKpiSummaryDto(
        int activeProjectCount,
        double averageProgressPercent,
        double budgetDeviationPercent,
        double scheduleCompliancePercent,
        int criticalRiskCount) {
}
