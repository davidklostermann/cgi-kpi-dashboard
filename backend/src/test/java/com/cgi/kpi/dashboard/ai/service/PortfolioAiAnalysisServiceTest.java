package com.cgi.kpi.dashboard.ai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cgi.kpi.dashboard.ai.cache.PortfolioAiAnalysisCache;
import com.cgi.kpi.dashboard.ai.config.AiActiveConfigProvider;
import com.cgi.kpi.dashboard.ai.config.AiProviderConfigVersionProvider;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.cgi.kpi.dashboard.ai.client.AiModelClient;
import com.cgi.kpi.dashboard.ai.config.AiProperties;
import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto;
import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto.EvidenceDto;
import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto.PortfolioInsightDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiQuestionResponseDto;
import com.cgi.kpi.dashboard.api.error.ApiException;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedPortfolioContextDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterOptionsDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiSummaryDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioStatusDistributionDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTableDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTableRowDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectReportTrendDto;
import com.cgi.kpi.dashboard.kpi.reader.PortfolioKpiReader;
import com.cgi.kpi.dashboard.kpi.reader.PortfolioReportTrendReader;
import com.cgi.kpi.dashboard.kpi.reader.PortfolioTableReader;
import com.cgi.kpi.dashboard.security.user.CurrentUserService;

class PortfolioAiAnalysisServiceTest {

    private static final UUID PROJECT_A = UUID.fromString("a0000000-0000-4000-8000-000000000001");
    private static final UUID PROJECT_B = UUID.fromString("a0000000-0000-4000-8000-000000000002");
    private static final UUID PROJECT_UNKNOWN = UUID.fromString("a0000000-0000-4000-8000-000000000099");

    private static final UUID TEST_USER_ID = UUID.fromString("40000000-0000-4000-8000-000000000004");
    private static final UUID TEST_WORKSPACE_ID = UUID.fromString("10000000-0000-4000-8000-000000000001");

    @Test
    void rejectsWithoutUserGeminiKey() {
        PortfolioAiAnalysisService service = serviceWithConfig(mockConfigProviderWithoutGeminiKey(), adminUserService());

        ApiException ex = assertThrows(
                ApiException.class, () -> service.analyzeTrend(PortfolioFilterCriteria.empty()));

        assertEquals("AI_KEY_MISSING", ex.getCode());
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void cachesAnalysisPerUserAndFilterCriteria() {
        AiActiveConfigProvider configProvider = mockConfigProviderWithGeminiKey();
        PortfolioAiAnalysisCache cache = new PortfolioAiAnalysisCache();
        PortfolioAiAnalysisService service = serviceWithConfig(configProvider, adminUserService(), cache);

        PortfolioTrendAnalysisResponseDto first = service.analyzeTrend(PortfolioFilterCriteria.empty());
        PortfolioTrendAnalysisResponseDto second = service.analyzeTrend(PortfolioFilterCriteria.empty());

        assertEquals(first.generatedAt(), second.generatedAt());
    }

    @Test
    void differentUsersDoNotShareCachedAnalysis() {
        AiActiveConfigProvider configProvider = mockConfigProviderWithGeminiKey();
        PortfolioAiAnalysisCache cache = new PortfolioAiAnalysisCache();
        CurrentUserService userService = mock(CurrentUserService.class);
        doNothing().when(userService).requireAdmin();
        when(userService.requireUserId()).thenReturn(TEST_USER_ID);
        when(userService.requireWorkspaceId()).thenReturn(TEST_WORKSPACE_ID);

        PortfolioAiAnalysisService service = serviceWithConfig(configProvider, userService, cache);
        service.analyzeTrend(PortfolioFilterCriteria.empty());

        UUID otherUserId = UUID.fromString("50000000-0000-4000-8000-000000000005");
        when(userService.requireUserId()).thenReturn(otherUserId);

        service.analyzeTrend(PortfolioFilterCriteria.empty());

        String userKey = PortfolioAiAnalysisCache.buildKey(
                TEST_USER_ID, TEST_WORKSPACE_ID, PortfolioFilterCriteria.empty(), 0L);
        String otherUserKey = PortfolioAiAnalysisCache.buildKey(
                otherUserId, TEST_WORKSPACE_ID, PortfolioFilterCriteria.empty(), 0L);
        assertEquals(true, cache.get(userKey) != null);
        assertEquals(true, cache.get(otherUserKey) != null);
        assertEquals(false, cache.get(userKey).generatedAt().equals(cache.get(otherUserKey).generatedAt()));
    }

    @Test
    void rejectsNonAdminPrincipal() {
        CurrentUserService userService = mock(CurrentUserService.class);
        doThrow(new ApiException("FORBIDDEN", "Admin role required", HttpStatus.FORBIDDEN))
                .when(userService)
                .requireAdmin();

        PortfolioAiAnalysisService service = serviceWithConfig(mockConfigProviderWithGeminiKey(), userService);

        ApiException ex = assertThrows(
                ApiException.class, () -> service.analyzeTrend(PortfolioFilterCriteria.empty()));

        assertEquals("FORBIDDEN", ex.getCode());
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void acceptsDeterministicMultiProjectInsightsAndDropsSingleProjectOnes() {
        AiModelClient client = emptyModelClient(List.of(
                singleProjectInsight(),
                validInsightFromModel()));

        PortfolioAiAnalysisService service = serviceWithConfig(
                mockConfigProviderWithGeminiKey(), adminUserService(), client);

        PortfolioTrendAnalysisResponseDto dto = service.analyzeTrend(PortfolioFilterCriteria.empty());
        assertTrue(dto.insights().stream().noneMatch(i -> i.affectedProjectIds().size() < 2));
        assertTrue(dto.insights().stream().anyMatch(i -> "DETERIORATING_TREND".equals(i.type())));
        assertTrue(dto.aiGenerated());
    }

    @Test
    void dropsInsightsWithoutEvidence() {
        AiModelClient client = emptyModelClient(List.of(new PortfolioInsightDto(
                "no-evidence",
                "REPORTING_PATTERN",
                "Titel",
                "Finding",
                "Implication",
                null,
                List.of(PROJECT_A, PROJECT_B),
                List.of("A", "B"),
                List.of(),
                "HIGH",
                "COMPLETE",
                Instant.now())));

        PortfolioAiAnalysisService service = serviceWithConfig(
                mockConfigProviderWithGeminiKey(),
                adminUserService(),
                client,
                summaryReader(),
                tableReader(PROJECT_A, PROJECT_B),
                ids -> List.of());

        PortfolioTrendAnalysisResponseDto dto = service.analyzeTrend(PortfolioFilterCriteria.empty());
        assertTrue(dto.insights().stream().noneMatch(i -> "no-evidence".equals(i.id())));
    }

    @Test
    void usesCanonicalProjectNamesFromBackend() {
        PortfolioAiAnalysisService service = serviceWithConfig(
                mockConfigProviderWithGeminiKey(), adminUserService(), emptyModelClient(List.of()));

        PortfolioTrendAnalysisResponseDto dto = service.analyzeTrend(PortfolioFilterCriteria.empty());
        assertTrue(dto.insights().size() >= 1);
        PortfolioInsightDto first = dto.insights().get(0);
        assertTrue(first.affectedProjectNames().contains("Nexus"));
        assertTrue(first.affectedProjectNames().contains("Atlas"));
    }

    @Test
    void refinesDetectorWordingFromMatchingModelInsightAndDropsLlmOnlyTypes() {
        AiModelClient client = emptyModelClient(List.of(
                new PortfolioInsightDto(
                        "llm-det",
                        "DETERIORATING_TREND",
                        "LLM-Titel",
                        "LLM-Finding",
                        "LLM-Implication",
                        "LLM-Action",
                        List.of(PROJECT_A, PROJECT_B),
                        List.of("X", "Y"),
                        List.of(
                                new EvidenceDto("fake", "999", PROJECT_A, LocalDate.of(2026, 7, 1), "fake.field"),
                                new EvidenceDto("fake2", "998", PROJECT_B, LocalDate.of(2026, 7, 1), "fake.field")),
                        "LOW",
                        "INSUFFICIENT",
                        Instant.now()),
                new PortfolioInsightDto(
                        "llm-only",
                        "REPORTING_PATTERN",
                        "Nur Modell",
                        "Finding",
                        "Implication",
                        null,
                        List.of(PROJECT_A, PROJECT_B),
                        List.of("Nexus", "Atlas"),
                        List.of(
                                new EvidenceDto(
                                        "a",
                                        "1",
                                        PROJECT_A,
                                        LocalDate.of(2026, 7, 1),
                                        "snapshot.openRiskCount"),
                                new EvidenceDto(
                                        "b",
                                        "2",
                                        PROJECT_B,
                                        LocalDate.of(2026, 7, 1),
                                        "snapshot.progressPercent")),
                        "HIGH",
                        "COMPLETE",
                        Instant.now())));

        // Trends produce DETERIORATING + REPORTING; LLM may refine DETERIORATING wording only.
        PortfolioAiAnalysisService service = serviceWithConfig(
                mockConfigProviderWithGeminiKey(), adminUserService(), client);

        PortfolioTrendAnalysisResponseDto dto = service.analyzeTrend(PortfolioFilterCriteria.empty());
        PortfolioInsightDto deteriorating = dto.insights().stream()
                .filter(i -> "DETERIORATING_TREND".equals(i.type()))
                .findFirst()
                .orElseThrow();
        assertEquals("LLM-Titel", deteriorating.title());
        assertEquals("LLM-Finding", deteriorating.finding());
        assertTrue(deteriorating.evidence().stream()
                .anyMatch(e -> "snapshot.scheduleDeviationDays".equals(e.sourceField())));
        assertTrue(dto.insights().stream().anyMatch(i -> "REPORTING_PATTERN".equals(i.type())));
    }

    @Test
    void detectorFallbackSetsAiGeneratedFalse() {
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
                throw new RuntimeException("boom");
            }
        };

        PortfolioAiAnalysisService service = serviceWithConfig(
                mockConfigProviderWithGeminiKey(), adminUserService(), client);

        PortfolioTrendAnalysisResponseDto dto = service.analyzeTrend(PortfolioFilterCriteria.empty());
        assertTrue(dto.insights().size() >= 1);
        assertTrue(!dto.aiGenerated());
    }

    private static PortfolioAiAnalysisService serviceWithConfig(
            AiActiveConfigProvider configProvider, CurrentUserService userService) {
        return serviceWithConfig(
                configProvider,
                userService,
                emptyModelClient(List.of()),
                summaryReader(),
                tableReader(PROJECT_A, PROJECT_B),
                trendsFor(PROJECT_A, PROJECT_B));
    }

    private static PortfolioAiAnalysisService serviceWithConfig(
            AiActiveConfigProvider configProvider,
            CurrentUserService userService,
            AiModelClient client) {
        return serviceWithConfig(
                configProvider,
                userService,
                client,
                summaryReader(),
                tableReader(PROJECT_A, PROJECT_B),
                trendsFor(PROJECT_A, PROJECT_B));
    }

    private static PortfolioAiAnalysisService serviceWithConfig(
            AiActiveConfigProvider configProvider,
            CurrentUserService userService,
            PortfolioAiAnalysisCache cache) {
        return serviceWithConfig(
                configProvider,
                userService,
                emptyModelClient(List.of()),
                summaryReader(),
                tableReader(PROJECT_A, PROJECT_B),
                trendsFor(PROJECT_A, PROJECT_B),
                cache);
    }

    private static PortfolioAiAnalysisService serviceWithConfig(
            AiActiveConfigProvider configProvider,
            CurrentUserService userService,
            AiModelClient client,
            PortfolioKpiReader kpiReader,
            PortfolioTableReader tableReader,
            PortfolioReportTrendReader trendReader) {
        return serviceWithConfig(
                configProvider, userService, client, kpiReader, tableReader, trendReader, new PortfolioAiAnalysisCache());
    }

    private static PortfolioAiAnalysisService serviceWithConfig(
            AiActiveConfigProvider configProvider,
            CurrentUserService userService,
            AiModelClient client,
            PortfolioKpiReader kpiReader,
            PortfolioTableReader tableReader,
            PortfolioReportTrendReader trendReader,
            PortfolioAiAnalysisCache cache) {
        when(userService.requireUserId()).thenReturn(TEST_USER_ID);
        when(userService.requireWorkspaceId()).thenReturn(TEST_WORKSPACE_ID);
        AiProviderConfigVersionProvider versionProvider = new AiProviderConfigVersionProvider(configProvider);
        return new PortfolioAiAnalysisService(
                kpiReader,
                tableReader,
                trendReader,
                new ApprovedPortfolioContextAssembler(),
                new PortfolioPatternDetector(),
                client,
                enabledProperties(),
                userService,
                cache,
                versionProvider,
                configProvider);
    }

    private static AiActiveConfigProvider mockConfigProviderWithGeminiKey() {
        AiActiveConfigProvider configProvider = mock(AiActiveConfigProvider.class);
        when(configProvider.getActiveApiKey("gemini")).thenReturn("mock-api-key");
        when(configProvider.isEnabled(anyString())).thenReturn(true);
        when(configProvider.getCurrentVersion()).thenReturn(0L);
        return configProvider;
    }

    private static AiActiveConfigProvider mockConfigProviderWithoutGeminiKey() {
        AiActiveConfigProvider configProvider = mock(AiActiveConfigProvider.class);
        when(configProvider.getActiveApiKey("gemini")).thenReturn(null);
        return configProvider;
    }

    private static PortfolioInsightDto singleProjectInsight() {
        return new PortfolioInsightDto(
                "single",
                "DETERIORATING_TREND",
                "Einzelprojekt",
                "Nur ein Projekt",
                "Implication",
                null,
                List.of(PROJECT_A),
                List.of("Nexus"),
                List.of(
                        new EvidenceDto("a", "1", PROJECT_A, LocalDate.of(2026, 7, 1), "x"),
                        new EvidenceDto("b", "2", PROJECT_A, LocalDate.of(2026, 7, 1), "y")),
                "HIGH",
                "COMPLETE",
                Instant.now());
    }

    private static PortfolioInsightDto validInsightFromModel() {
        return new PortfolioInsightDto(
                "model-reporting",
                "REPORTING_PATTERN",
                "Modell-Muster",
                "Finding",
                "Implication",
                null,
                List.of(PROJECT_A, PROJECT_UNKNOWN),
                List.of("Nexus", "Unknown"),
                List.of(
                        new EvidenceDto("a", "1", PROJECT_A, LocalDate.of(2026, 7, 1), "x"),
                        new EvidenceDto("b", "2", PROJECT_B, LocalDate.of(2026, 7, 1), "y")),
                "MEDIUM",
                "PARTIAL",
                Instant.now());
    }

    private static AiModelClient emptyModelClient(List<PortfolioInsightDto> insights) {
        return new AiModelClient() {
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
                return new PortfolioTrendAnalysisResponseDto(insights, true, "Disclaimer", Instant.now());
            }
        };
    }

    private static AiProperties enabledProperties() {
        AiProperties properties = new AiProperties();
        properties.setEnabled(true);
        properties.setProvider("mock");
        return properties;
    }

    private static CurrentUserService adminUserService() {
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        doNothing().when(currentUserService).requireAdmin();
        when(currentUserService.requireUserId()).thenReturn(TEST_USER_ID);
        when(currentUserService.requireWorkspaceId()).thenReturn(TEST_WORKSPACE_ID);
        return currentUserService;
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

    private static PortfolioTableReader tableReader(UUID first, UUID second) {
        return criteria -> new PortfolioTableDto(
                List.of(row(first, "Nexus", "CRITICAL"), row(second, "Atlas", "AT_RISK")),
                false);
    }

    private static PortfolioTableRowDto row(UUID id, String name, String status) {
        return new PortfolioTableRowDto(
                id,
                name,
                "Acme",
                "Lead",
                status,
                status,
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
                Instant.parse("2026-07-10T08:00:00Z"));
    }

    private static PortfolioReportTrendReader trendsFor(UUID first, UUID second) {
        return (Collection<UUID> ids) -> List.of(
                trend(first, "Nexus", 10, 14, "ON_TRACK", "AT_RISK", 0, 1),
                trend(second, "Atlas", 8, 12, "AT_RISK", "CRITICAL", 1, 2));
    }

    private static ProjectReportTrendDto trend(
            UUID id,
            String name,
            int prevDev,
            int currDev,
            String prevStatus,
            String currStatus,
            int prevRisks,
            int currRisks) {
        return new ProjectReportTrendDto(
                id,
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
