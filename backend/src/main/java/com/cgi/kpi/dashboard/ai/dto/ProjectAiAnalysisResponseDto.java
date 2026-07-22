package com.cgi.kpi.dashboard.ai.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProjectAiAnalysisResponseDto(
        UUID projectId,
        Instant factsAsOf,
        Instant generatedAt,
        String status,
        List<String> availableSources,
        String summary,
        List<PriorityDto> priorities,
        List<SuggestedActionDto> suggestedActions,
        List<MissingDataDto> missingData,
        boolean aiGenerated,
        String disclaimer) {

    public record EvidenceItemDto(
            String label,
            String value,
            String sourceField) {
    }

    public record PriorityDto(
            int rank,
            String title,
            String managementImplication,
            String requiredDecision,
            List<EvidenceItemDto> evidence,
            List<String> evidenceFactIds) {
    }

    public record SuggestedActionDto(
            String title,
            String reason,
            String suggestedOwner,
            String suggestedDueDate,
            String addressesType,
            String addressesId,
            String expectedEffect,
            List<String> evidenceFactIds,
            boolean isProposal) {
    }

    public record MissingDataDto(
            String area,
            String description) {
    }
}
