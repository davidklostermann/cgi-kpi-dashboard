package com.cgi.kpi.dashboard.kpi.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Approved project facts for AI consumption only (FR-13 / Story 9.1).
 * No JPA entities — stable factIds for evidence validation.
 */
public record ApprovedProjectContextDto(
        UUID projectId,
        String projectName,
        Instant factsAsOf,
        List<ApprovedProjectFactDto> facts,
        List<MissingDataItemDto> missingData) {

    public record ApprovedProjectFactDto(
            String factId,
            String category,
            String label,
            Object value,
            String displayValue,
            String sourceEntityType,
            String sourceEntityId,
            String detailAnchor) {
    }

    public record MissingDataItemDto(
            String area,
            String description) {
    }
}
