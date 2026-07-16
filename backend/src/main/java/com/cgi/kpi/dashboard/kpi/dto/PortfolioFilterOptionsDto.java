package com.cgi.kpi.dashboard.kpi.dto;

import java.util.List;

/**
 * Distinct filter values for portfolio UI controls (FR-8).
 */
public record PortfolioFilterOptionsDto(
        List<String> customers,
        List<String> projectLeads,
        List<String> phases,
        List<String> reportMonths,
        List<String> statuses,
        List<String> riskSeverities) {
}
