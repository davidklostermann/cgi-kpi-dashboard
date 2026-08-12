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
            String cause,
            String impact,
            String severity,
            String severityLabel,
            String priority,
            List<MetricDto> metrics,
            String owner,
            LocalDate dueDate,
            Integer overdueDays,
            String overdueLabel,
            String nextAction,
            boolean escalationNeeded,
            String actionKind,
            String actionLabel,
            String actionText,
            RequiredDecisionDto requiredDecision) {
    }

    /**
     * Management decision needed for an issue — only present when seed/action text indicates a decision gate.
     */
    public record RequiredDecisionDto(
            String decideWho,
            LocalDate decideBy,
            String impactIfDeferred) {
    }

    public record MetricDto(String label, String value) {
    }
}
