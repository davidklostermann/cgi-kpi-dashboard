package com.cgi.kpi.dashboard.kpi.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Freigegebene Portfolio-Fakten für {@code ai.*} (AD-2 / Story 8.1).
 */
public record ApprovedPortfolioContextDto(
        Instant factsAsOf,
        List<ApprovedPortfolioFactDto> facts,
        List<CandidateProjectDto> candidateProjects) {

    public record ApprovedPortfolioFactDto(
            String factId,
            String category,
            String label,
            Object value,
            String displayValue) {
    }

    public record CandidateProjectDto(
            UUID projectId,
            String projectName,
            String status,
            String statusLabel,
            int progressPercent,
            Integer scheduleDeviationDays,
            Double budgetDeviationPercent,
            int openRiskCount,
            int criticalIssueCount,
            List<String> evidenceFactIds) {
    }
}
