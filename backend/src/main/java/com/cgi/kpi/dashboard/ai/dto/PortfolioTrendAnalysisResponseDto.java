package com.cgi.kpi.dashboard.ai.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Portfolio pattern analysis response (FR-4 / Epic 8 — Insight-Modell).
 */
public record PortfolioTrendAnalysisResponseDto(
        List<PortfolioInsightDto> insights,
        boolean aiGenerated,
        String disclaimer,
        Instant generatedAt) {

    public record PortfolioInsightDto(
            String id,
            String type,
            String title,
            String finding,
            String managementImplication,
            String recommendedAction,
            List<UUID> affectedProjectIds,
            List<String> affectedProjectNames,
            List<EvidenceDto> evidence,
            String confidence,
            String dataQuality,
            Instant detectedAt) {
    }

    public record EvidenceDto(
            String label,
            String value,
            UUID projectId,
            LocalDate reportDate,
            String sourceField) {
    }
}
