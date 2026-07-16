package com.cgi.kpi.dashboard.kpi.dto;

/**
 * Aggregated portfolio KPI values for API and AI reader surfaces (FR-1).
 */
public record PortfolioKpiSummaryDto(
        int activeProjectCount,
        double averageProgressPercent,
        double budgetDeviationPercent,
        double scheduleCompliancePercent,
        int criticalRiskCount,
        PortfolioStatusDistributionDto statusDistribution,
        boolean empty) {

    public static PortfolioKpiSummaryDto emptyPortfolio() {
        return new PortfolioKpiSummaryDto(
                0,
                0.0,
                0.0,
                0.0,
                0,
                new PortfolioStatusDistributionDto(0, 0, 0, 0),
                true);
    }
}
