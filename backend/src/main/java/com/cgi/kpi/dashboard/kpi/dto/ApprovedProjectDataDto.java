package com.cgi.kpi.dashboard.kpi.dto;

import java.util.UUID;

/**
 * Freigegebene Projektdaten für KPI-Endpunkte und {@code ai.*} (FR-14).
 */
public record ApprovedProjectDataDto(
        UUID projectId,
        String projectName,
        String customerName,
        String statusWord,
        int progressPercent,
        Integer scheduleDeviationDays,
        double budgetUtilizationPercent,
        double effortUtilizationPercent) {
}
