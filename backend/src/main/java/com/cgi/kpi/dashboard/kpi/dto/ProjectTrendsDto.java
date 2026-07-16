package com.cgi.kpi.dashboard.kpi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Report snapshot comparison deltas (FR-21 / Story 6.7).
 */
public record ProjectTrendsDto(
        UUID projectId,
        boolean comparisonAvailable,
        String unavailableReason,
        LocalDate previousSnapshotDate,
        LocalDate currentSnapshotDate,
        Integer progressDeltaPercent,
        BigDecimal budgetActualDelta,
        Integer scheduleDeviationDeltaDays,
        String previousStatus,
        String previousStatusLabel,
        String currentStatus,
        String currentStatusLabel,
        Integer openRiskCountDelta) {
}
