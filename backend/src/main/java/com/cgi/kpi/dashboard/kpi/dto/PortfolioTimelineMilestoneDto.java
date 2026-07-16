package com.cgi.kpi.dashboard.kpi.dto;

import java.time.LocalDate;

/**
 * Milestone marker for portfolio Gantt timeline (FR-3 / Story 5.1).
 */
public record PortfolioTimelineMilestoneDto(
        String name,
        LocalDate dueDate,
        LocalDate completedDate,
        String status,
        String statusLabel) {
}
