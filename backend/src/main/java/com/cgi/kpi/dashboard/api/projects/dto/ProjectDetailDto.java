package com.cgi.kpi.dashboard.api.projects.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Project master data for detail views (FR-5 basis — Story 3.5).
 */
public record ProjectDetailDto(
        UUID id,
        String name,
        String customerName,
        String status,
        String startDate,
        String plannedEndDate,
        String actualEndDate,
        int progressPercent,
        Integer scheduleDeviationDays,
        Instant createdAt) {
}
