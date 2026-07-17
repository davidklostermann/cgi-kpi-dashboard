package com.cgi.kpi.dashboard.ai.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Portfolio KI trend analysis response (FR-4 / Epic 8).
 */
public record PortfolioTrendAnalysisResponseDto(
        String text,
        boolean aiGenerated,
        String disclaimer,
        Instant generatedAt,
        List<TopProjectDto> topProjects) {

    public record TopProjectDto(
            UUID projectId,
            String projectName,
            String reason,
            List<String> evidenceFactIds) {
    }
}
