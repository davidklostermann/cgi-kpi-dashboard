package com.cgi.kpi.dashboard.kpi.dto;

/**
 * One aggregated portfolio trend point per period (FR-3 / Story 5.4).
 */
public record PortfolioTrendPointDto(
        String period,
        double averageProgressPercent,
        double totalActualBudget) {
}
