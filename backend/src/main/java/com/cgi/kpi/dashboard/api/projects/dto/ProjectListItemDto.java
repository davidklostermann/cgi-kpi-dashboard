package com.cgi.kpi.dashboard.api.projects.dto;

import java.util.UUID;

/**
 * Project row for portfolio table and list views (FR-2 basis — Story 3.5).
 */
public record ProjectListItemDto(
        UUID id,
        String name,
        String customerName,
        String status,
        int progressPercent,
        Integer scheduleDeviationDays,
        String plannedEndDate) {
}
