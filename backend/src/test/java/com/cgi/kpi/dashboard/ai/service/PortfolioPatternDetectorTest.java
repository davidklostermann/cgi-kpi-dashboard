package com.cgi.kpi.dashboard.ai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto.PortfolioInsightDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectReportTrendDto;

class PortfolioPatternDetectorTest {

    private final PortfolioPatternDetector detector = new PortfolioPatternDetector();

    @Test
    void detectsDeterioratingTrendAndReportingPatternForMultiProjectSeedShape() {
        List<PortfolioInsightDto> insights = detector.detect(List.of(
                trend("a0000000-0000-4000-8000-000000000002", "P2", 19, 21, "AT_RISK", "AT_RISK", 0, 1),
                trend("a0000000-0000-4000-8000-000000000003", "P3", 43, 45, "CRITICAL", "CRITICAL", 0, 1),
                trend("a0000000-0000-4000-8000-000000000011", "P11", 4, 6, "ON_TRACK", "AT_RISK", 0, 0)));

        assertEquals(2, insights.size());
        assertTrue(insights.stream().anyMatch(i -> "DETERIORATING_TREND".equals(i.type())));
        assertTrue(insights.stream().anyMatch(i -> "REPORTING_PATTERN".equals(i.type())));
        insights.forEach(insight -> {
            assertTrue(insight.affectedProjectIds().size() >= 2);
            assertTrue(insight.evidence().size() >= 2);
            assertTrue(insight.evidence().stream().noneMatch(e -> e.sourceField().contains("actualBudget")));
        });
    }

    @Test
    void ignoresNullScheduleDeviationAndUnknownStatusForDeterioration() {
        List<PortfolioInsightDto> insights = detector.detect(List.of(
                trend("a0000000-0000-4000-8000-000000000002", "P2", null, 5, "WEIRD", "ON_TRACK", 0, 0),
                trend("a0000000-0000-4000-8000-000000000003", "P3", null, 8, "WEIRD", "ON_TRACK", 0, 0)));
        assertTrue(insights.stream().noneMatch(i -> "DETERIORATING_TREND".equals(i.type())));
    }

    @Test
    void returnsNoInsightWhenFewerThanTwoProjectsMatch() {
        List<PortfolioInsightDto> insights = detector.detect(List.of(
                trend("a0000000-0000-4000-8000-000000000002", "P2", 19, 21, "AT_RISK", "AT_RISK", 0, 1)));
        assertTrue(insights.isEmpty());
    }

    private static ProjectReportTrendDto trend(
            String id,
            String name,
            Integer prevDev,
            Integer currDev,
            String prevStatus,
            String currStatus,
            int prevRisks,
            int currRisks) {
        return new ProjectReportTrendDto(
                UUID.fromString(id),
                name,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 7, 1),
                prevDev,
                currDev,
                prevStatus,
                currStatus,
                prevRisks,
                currRisks,
                new BigDecimal("100"),
                new BigDecimal("120"),
                40,
                45);
    }
}
