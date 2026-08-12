package com.cgi.kpi.dashboard.kpi.dto;

import java.util.List;

/**
 * Optional portfolio filter criteria (FR-8) — query parameters on KPI endpoints.
 */
public record PortfolioFilterCriteria(
        String customer,
        String projectLead,
        List<String> statuses,
        String phase,
        LifecycleFilter lifecycle,
        String reportMonth,
        String riskSeverity,
        List<String> deliveryMethods) {

    public enum LifecycleFilter {
        ACTIVE,
        COMPLETED,
        ALL
    }

    public static PortfolioFilterCriteria empty() {
        return new PortfolioFilterCriteria(
                null, null, List.of(), null, LifecycleFilter.ACTIVE, null, null, List.of());
    }

    public boolean hasAnyFilter() {
        return (customer != null && !customer.isBlank())
                || (projectLead != null && !projectLead.isBlank())
                || (statuses != null && !statuses.isEmpty())
                || (phase != null && !phase.isBlank())
                || lifecycle == LifecycleFilter.COMPLETED
                || (reportMonth != null && !reportMonth.isBlank())
                || (riskSeverity != null && !riskSeverity.isBlank())
                || (deliveryMethods != null && !deliveryMethods.isEmpty());
    }
}
