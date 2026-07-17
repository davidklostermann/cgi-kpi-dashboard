package com.cgi.kpi.dashboard.kpi.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Operational problems, risks and countermeasures for the project detail facts column.
 */
public record ProjectIssuesActionsDto(
        UUID projectId,
        String factsBadge,
        Instant factsAsOf,
        List<IssueActionItemDto> items) {

    public record IssueActionItemDto(
            UUID id,
            String itemType,
            String itemTypeLabel,
            String category,
            String title,
            String description,
            String severity,
            String severityLabel,
            List<MetricDto> metrics,
            String owner,
            LocalDate dueDate,
            String actionKind,
            String actionLabel,
            String actionText) {
    }

    public record MetricDto(String label, String value) {
    }
}
