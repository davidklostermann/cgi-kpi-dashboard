package com.cgi.kpi.dashboard.kpi.dto;

import java.util.List;
import java.util.UUID;

/**
 * Deterministic management insights (FR-20 / Story 6.6).
 */
public record ProjectInsightsDto(
        UUID projectId,
        List<ProjectInsightItemDto> insights) {

    public record ProjectInsightItemDto(
            String code,
            String statement,
            String metrics,
            String comparisonValue,
            String period,
            String rationale,
            String type) {
    }
}
