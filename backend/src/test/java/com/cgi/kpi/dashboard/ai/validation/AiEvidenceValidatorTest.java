package com.cgi.kpi.dashboard.ai.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.cgi.kpi.dashboard.ai.dto.ProjectAiQuestionResponseDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto.ApprovedProjectFactDto;

class AiEvidenceValidatorTest {

    private final AiEvidenceValidator validator = new AiEvidenceValidator();

    @Test
    void allowsConversationalAnswerWithoutEvidence() {
        ProjectAiQuestionResponseDto raw = new ProjectAiQuestionResponseDto(
                "Hallo! Wobei kann ich helfen?",
                List.of(),
                Instant.parse("2026-07-01T08:00:00Z"),
                Instant.parse("2026-07-01T09:00:00Z"),
                false,
                true,
                "Disclaimer");

        ProjectAiQuestionResponseDto validated = validator.validateQuestion(context(), raw);

        assertEquals("Hallo! Wobei kann ich helfen?", validated.answer());
        assertTrue(validated.evidenceFactIds().isEmpty());
        assertFalse(validated.insufficientEvidence());
    }

    @Test
    void keepsInsufficientFlagForUnanswerableProjectQuestion() {
        ProjectAiQuestionResponseDto raw = new ProjectAiQuestionResponseDto(
                "Dazu liegen keine ausreichend konkreten freigegebenen Projektdaten vor.",
                List.of(),
                Instant.parse("2026-07-01T08:00:00Z"),
                Instant.parse("2026-07-01T09:00:00Z"),
                true,
                true,
                "Disclaimer");

        ProjectAiQuestionResponseDto validated = validator.validateQuestion(context(), raw);

        assertTrue(validated.insufficientEvidence());
        assertTrue(validated.evidenceFactIds().isEmpty());
    }

    private static ApprovedProjectContextDto context() {
        return new ApprovedProjectContextDto(
                UUID.fromString("a0000000-0000-4000-8000-000000000001"),
                "Nexus",
                Instant.parse("2026-07-01T08:00:00Z"),
                List.of(new ApprovedProjectFactDto(
                        "kpi.progressPercent",
                        "KPI",
                        "Fortschritt",
                        62,
                        "62 %",
                        "Project",
                        null,
                        "fact-kpis")),
                List.of());
    }
}
