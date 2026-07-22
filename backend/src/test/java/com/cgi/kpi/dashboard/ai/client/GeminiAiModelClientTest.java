package com.cgi.kpi.dashboard.ai.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiQuestionResponseDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedPortfolioContextDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedPortfolioContextDto.ApprovedPortfolioFactDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedPortfolioContextDto.CandidateProjectDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto.ApprovedProjectFactDto;
import com.fasterxml.jackson.databind.ObjectMapper;

class GeminiAiModelClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void analyzeParsesStructuredJsonWithoutLiveCall() {
        GeminiAiModelClient client = new GeminiAiModelClient(
                prompt -> """
                        {
                          "summary": "Status beobachten",
                          "priorities": [
                            {
                              "rank":1,
                              "title":"Fortschritt",
                              "managementImplication":"Beobachten",
                              "requiredDecision":"Keine Sofortentscheidung",
                              "evidence":[
                                {"label":"Fortschritt","value":"62 %","sourceField":"kpi.progressPercent"},
                                {"label":"Status","value":"Auf Kurs","sourceField":"project.status"}
                              ],
                              "evidenceFactIds":["kpi.progressPercent","project.status"]
                            }
                          ],
                          "suggestedActions": [
                            {"title":"Review","reason":"prüfen","suggestedOwner":"PL","evidenceFactIds":["kpi.progressPercent"],"expectedEffect":"Klarheit"}
                          ],
                          "missingData": []
                        }
                        """,
                objectMapper);

        ProjectAiAnalysisResponseDto dto = client.analyze(projectContext());
        assertEquals("Status beobachten", dto.summary());
        assertEquals(1, dto.priorities().size());
        assertEquals("kpi.progressPercent", dto.priorities().get(0).evidenceFactIds().get(0));
        assertTrue(dto.aiGenerated());
    }

    @Test
    void answerRejectsEmptyEvidenceAsInsufficient() {
        GeminiAiModelClient client = new GeminiAiModelClient(
                prompt -> """
                        {"answer":"Unklar","evidenceFactIds":[],"insufficientEvidence":true}
                        """,
                objectMapper);

        ProjectAiQuestionResponseDto dto = client.answer(projectContext(), "Wie ist Mars?");
        assertTrue(dto.insufficientEvidence());
        assertTrue(dto.evidenceFactIds().isEmpty());
    }

    @Test
    void invalidJsonThrowsTransportException() {
        GeminiAiModelClient client = new GeminiAiModelClient(prompt -> "not-json", objectMapper);
        assertThrows(GeminiTransportException.class, () -> client.analyze(projectContext()));
    }

    @Test
    void analyzePortfolioParsesInsights() {
        UUID id = UUID.fromString("a0000000-0000-4000-8000-000000000001");
        UUID id2 = UUID.fromString("a0000000-0000-4000-8000-000000000002");
        GeminiAiModelClient client = new GeminiAiModelClient(
                prompt -> """
                        {
                          "insights": [
                            {
                              "id": "t1",
                              "type": "DETERIORATING_TREND",
                              "title": "Trend",
                              "finding": "Finding",
                              "managementImplication": "Implication",
                              "recommendedAction": null,
                              "affectedProjectIds": [
                                "a0000000-0000-4000-8000-000000000001",
                                "a0000000-0000-4000-8000-000000000002"
                              ],
                              "affectedProjectNames": ["Nexus", "Atlas"],
                              "evidence": [
                                {"label":"A","value":"1","projectId":"a0000000-0000-4000-8000-000000000001","reportDate":"2026-07-01","sourceField":"x"},
                                {"label":"B","value":"2","projectId":"a0000000-0000-4000-8000-000000000002","reportDate":"2026-07-01","sourceField":"y"}
                              ],
                              "confidence": "HIGH",
                              "dataQuality": "COMPLETE"
                            }
                          ]
                        }
                        """,
                objectMapper);

        PortfolioTrendAnalysisResponseDto dto = client.analyzePortfolio(portfolioContext(id, id2));
        assertEquals(1, dto.insights().size());
        assertEquals(id, dto.insights().get(0).affectedProjectIds().get(0));
    }

    @Test
    void parseJsonExtractsNestedJsonFromMarkdownFence() {
        String fenced =
                """
                ```json
                {"outer":{"inner":1},"list":[{"k":"v"}]}
                ```
                """;
        assertEquals("{\"outer\":{\"inner\":1},\"list\":[{\"k\":\"v\"}]}", GeminiAiModelClient.extractJsonPayload(fenced));
    }

    private static ApprovedProjectContextDto projectContext() {
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

    private static ApprovedPortfolioContextDto portfolioContext(UUID projectId, UUID secondId) {
        return new ApprovedPortfolioContextDto(
                Instant.parse("2026-07-01T08:00:00Z"),
                List.of(new ApprovedPortfolioFactDto(
                        "portfolio.criticalRiskCount", "RISK", "Kritische Risiken", 4, "4")),
                List.of(
                        new CandidateProjectDto(
                                projectId,
                                "Nexus",
                                "CRITICAL",
                                "Kritisch",
                                40,
                                12,
                                8.0,
                                2,
                                1,
                                List.of("portfolio.criticalRiskCount")),
                        new CandidateProjectDto(
                                secondId,
                                "Atlas",
                                "AT_RISK",
                                "Beobachten",
                                48,
                                21,
                                5.0,
                                1,
                                0,
                                List.of("portfolio.criticalRiskCount"))));
    }
}
