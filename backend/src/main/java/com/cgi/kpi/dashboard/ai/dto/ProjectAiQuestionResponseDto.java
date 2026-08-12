package com.cgi.kpi.dashboard.ai.dto;

import java.time.Instant;
import java.util.List;

public record ProjectAiQuestionResponseDto(
        String answer,
        List<String> evidenceFactIds,
        Instant factsAsOf,
        Instant generatedAt,
        boolean insufficientEvidence,
        boolean aiGenerated,
        String disclaimer) {
}
