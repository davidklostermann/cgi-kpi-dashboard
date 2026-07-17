package com.cgi.kpi.dashboard.ai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.cgi.kpi.dashboard.ai.client.AiModelClient;
import com.cgi.kpi.dashboard.ai.config.AiProperties;
import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto;
import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto.TopProjectDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiQuestionResponseDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedPortfolioContextDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterOptionsDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiSummaryDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioStatusDistributionDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTableDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTableRowDto;
import com.cgi.kpi.dashboard.kpi.reader.PortfolioKpiReader;
import com.cgi.kpi.dashboard.kpi.reader.PortfolioTableReader;

class PortfolioAiAnalysisServiceTest {

    @Test
    void dropsUnknownFactIdsAndUnknownProjects() {
        UUID known = UUID.fromString("a0000000-0000-4000-8000-000000000001");
        UUID unknown = UUID.fromString("a0000000-0000-4000-8000-000000000099");

        AiModelClient client = new AiModelClient() {
            @Override
            public ProjectAiAnalysisResponseDto analyze(ApprovedProjectContextDto context) {
                throw new UnsupportedOperationException();
            }

            @Override
            public ProjectAiQuestionResponseDto answer(ApprovedProjectContextDto context, String question) {
                throw new UnsupportedOperationException();
            }

            @Override
            public PortfolioTrendAnalysisResponseDto analyzePortfolio(ApprovedPortfolioContextDto context) {
                return new PortfolioTrendAnalysisResponseDto(
                        "Text",
                        true,
                        "Disclaimer",
                        Instant.now(),
                        List.of(
                                new TopProjectDto(unknown, "X", "reason", List.of("portfolio.criticalRiskCount")),
                                new TopProjectDto(known, "Nexus", "reason", List.of("unknown.fact", "portfolio.criticalRiskCount"))));
            }
        };

        PortfolioAiAnalysisService service = new PortfolioAiAnalysisService(
                summaryReader(),
                tableReader(known),
                new ApprovedPortfolioContextAssembler(),
                client,
                enabledProperties());

        PortfolioTrendAnalysisResponseDto dto = service.analyzeTrend(PortfolioFilterCriteria.empty());
        assertEquals(1, dto.topProjects().size());
        assertEquals(known, dto.topProjects().get(0).projectId());
        assertEquals(List.of("portfolio.criticalRiskCount"), dto.topProjects().get(0).evidenceFactIds());
        assertTrue(dto.aiGenerated());
    }

    private static AiProperties enabledProperties() {
        AiProperties properties = new AiProperties();
        properties.setEnabled(true);
        properties.setProvider("mock");
        return properties;
    }

    private static PortfolioKpiReader summaryReader() {
        return new PortfolioKpiReader() {
            @Override
            public PortfolioKpiSummaryDto readPortfolioSummary(PortfolioFilterCriteria criteria) {
                return new PortfolioKpiSummaryDto(
                        2, 50, 3, 70, 4, new PortfolioStatusDistributionDto(1, 0, 1, 0), false);
            }

            @Override
            public PortfolioFilterOptionsDto readFilterOptions() {
                return new PortfolioFilterOptionsDto(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
            }
        };
    }

    private static PortfolioTableReader tableReader(UUID projectId) {
        return criteria -> new PortfolioTableDto(
                List.of(new PortfolioTableRowDto(
                        projectId,
                        "Nexus",
                        "Acme",
                        "Lead",
                        "CRITICAL",
                        "Kritisch",
                        "Umsetzung",
                        40,
                        null,
                        null,
                        12,
                        100.0,
                        8.0,
                        0.0,
                        2,
                        1,
                        Instant.parse("2026-07-10T08:00:00Z"))),
                false);
    }
}
