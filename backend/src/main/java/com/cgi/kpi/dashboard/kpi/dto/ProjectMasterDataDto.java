package com.cgi.kpi.dashboard.kpi.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Project master data for detail header (FR-5 / Story 6.2).
 */
public record ProjectMasterDataDto(
        UUID id,
        String name,
        String customer,
        String projectLead,
        LocalDate startDate,
        LocalDate plannedEndDate,
        LocalDate forecastEndDate,
        String currentPhaseName,
        String status,
        String statusLabel,
        Instant lastDataUpdate) {
}
