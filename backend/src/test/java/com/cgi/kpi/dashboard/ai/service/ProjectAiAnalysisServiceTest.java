package com.cgi.kpi.dashboard.ai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.cgi.kpi.dashboard.ai.config.AiActiveConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.cgi.kpi.dashboard.ai.cache.ProjectAiAnalysisCache;
import com.cgi.kpi.dashboard.ai.client.AiModelClient;
import com.cgi.kpi.dashboard.ai.client.GeminiTransportException;
import com.cgi.kpi.dashboard.ai.config.AiProperties;
import com.cgi.kpi.dashboard.ai.config.AiProviderConfigVersionProvider;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto;
import com.cgi.kpi.dashboard.ai.validation.AiEvidenceValidator;
import com.cgi.kpi.dashboard.api.error.ApiException;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto.ApprovedProjectFactDto;
import com.cgi.kpi.dashboard.kpi.reader.ApprovedProjectDataReader;
import com.cgi.kpi.dashboard.security.user.CurrentUserService;

class ProjectAiAnalysisServiceTest {

    private static final UUID PROJECT_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");
    private static final UUID WORKSPACE_A = UUID.fromString("10000000-0000-4000-8000-000000000001");
    private static final UUID WORKSPACE_B = UUID.fromString("20000000-0000-4000-8000-000000000002");

    private ApprovedProjectDataReader reader;
    private AiModelClient modelClient;
    private AiProperties properties;
    private CurrentUserService currentUserService;
    private ProjectAiAnalysisCache cache;
    private AiProviderConfigVersionProvider configVersionProvider;
    private AiActiveConfigProvider configProvider;
    private ProjectAiAnalysisService service;

    @BeforeEach
    void setUp() {
        reader = mock(ApprovedProjectDataReader.class);
        modelClient = mock(AiModelClient.class);
        properties = new AiProperties();
        properties.setEnabled(true);
        currentUserService = mock(CurrentUserService.class);
        doNothing().when(currentUserService).requireAdmin();
        when(currentUserService.requireWorkspaceId()).thenReturn(WORKSPACE_A);
        cache = new ProjectAiAnalysisCache();
        configProvider = mock(AiActiveConfigProvider.class);
        when(configProvider.getCurrentVersion()).thenReturn(0L);
        configVersionProvider = new AiProviderConfigVersionProvider(configProvider);
        service = new ProjectAiAnalysisService(
                reader,
                modelClient,
                new AiEvidenceValidator(),
                properties,
                currentUserService,
                cache,
                configVersionProvider);
        when(reader.readApprovedContext(PROJECT_ID)).thenReturn(Optional.of(context()));
        when(modelClient.analyze(any())).thenReturn(validAnalysis());
    }

    @Test
    void rejectsNonAdminOnAnalyze() {
        doThrow(new ApiException("FORBIDDEN", "Admin role required", HttpStatus.FORBIDDEN))
                .when(currentUserService)
                .requireAdmin();

        ApiException ex = assertThrows(ApiException.class, () -> service.analyze(PROJECT_ID, false));

        assertEquals("FORBIDDEN", ex.getCode());
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void rejectsNonAdminOnAsk() {
        doThrow(new ApiException("FORBIDDEN", "Admin role required", HttpStatus.FORBIDDEN))
                .when(currentUserService)
                .requireAdmin();

        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.ask(
                        PROJECT_ID, new com.cgi.kpi.dashboard.ai.dto.ProjectAiQuestionRequestDto("Status?")));

        assertEquals("FORBIDDEN", ex.getCode());
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void disabledFeatureReturnsAiDisabled() {
        properties.setEnabled(false);

        ApiException ex = assertThrows(ApiException.class, () -> service.analyze(PROJECT_ID, false));

        assertEquals("AI_DISABLED", ex.getCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatus());
    }

    @Test
    void provider403ReturnsAiProviderError() {
        when(modelClient.analyze(any())).thenThrow(new GeminiTransportException("Gemini HTTP 403", 403));

        ApiException ex = assertThrows(ApiException.class, () -> service.analyze(PROJECT_ID, true));

        assertEquals("AI_PROVIDER_ERROR", ex.getCode());
    }

    @Test
    void provider404ReturnsAiProviderError() {
        when(modelClient.analyze(any())).thenThrow(new GeminiTransportException("Gemini HTTP 404", 404));

        ApiException ex = assertThrows(ApiException.class, () -> service.analyze(PROJECT_ID, true));

        assertEquals("AI_PROVIDER_ERROR", ex.getCode());
    }

    @Test
    void rejectsQuestionExceedingMaxLength() {
        String longQuestion = "x".repeat(ProjectAiAnalysisService.MAX_QUESTION_LENGTH + 1);

        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.ask(PROJECT_ID, new com.cgi.kpi.dashboard.ai.dto.ProjectAiQuestionRequestDto(longQuestion)));

        assertEquals("BAD_REQUEST", ex.getCode());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void cachesAnalysisWithinSameWorkspace() {
        ProjectAiAnalysisResponseDto first = service.analyze(PROJECT_ID, false);
        ProjectAiAnalysisResponseDto second = service.analyze(PROJECT_ID, false);

        assertSame(first, second);
        verify(modelClient, times(1)).analyze(any());
    }

    @Test
    void refreshBypassesCache() {
        service.analyze(PROJECT_ID, false);
        service.analyze(PROJECT_ID, true);

        verify(modelClient, times(2)).analyze(any());
    }

    @Test
    void differentWorkspacesDoNotShareCache() {
        service.analyze(PROJECT_ID, false);

        when(currentUserService.requireWorkspaceId()).thenReturn(WORKSPACE_B);
        service.analyze(PROJECT_ID, false);

        verify(modelClient, times(2)).analyze(any());
    }

    @Test
    void configVersionChangeBypassesStaleCache() {
        service.analyze(PROJECT_ID, false);

        when(configProvider.getCurrentVersion()).thenReturn(1L);
        service.analyze(PROJECT_ID, false);

        verify(modelClient, times(2)).analyze(any());
    }

    @Test
    void invalidateAllForcesFreshAnalysis() {
        service.analyze(PROJECT_ID, false);

        cache.invalidateAll();
        service.analyze(PROJECT_ID, false);

        verify(modelClient, times(2)).analyze(any());
    }

    private static ApprovedProjectContextDto context() {
        return new ApprovedProjectContextDto(
                PROJECT_ID,
                "Test Project",
                Instant.parse("2026-07-01T08:00:00Z"),
                List.of(new ApprovedProjectFactDto(
                        "kpi.progressPercent",
                        "KPI",
                        "Fortschritt",
                        62,
                        "62 %",
                        "kpi",
                        "kpi-1",
                        "fact-kpis")),
                List.of());
    }

    private static ProjectAiAnalysisResponseDto validAnalysis() {
        Instant factsAsOf = Instant.parse("2026-07-01T08:00:00Z");
        return new ProjectAiAnalysisResponseDto(
                PROJECT_ID,
                factsAsOf,
                Instant.parse("2026-07-01T09:00:00Z"),
                "OK",
                List.of("kpi"),
                "Summary based on approved facts.",
                List.of(new ProjectAiAnalysisResponseDto.PriorityDto(
                        1,
                        "Fortschritt prüfen",
                        "Management should review progress.",
                        "Review KPI trend",
                        List.of(new ProjectAiAnalysisResponseDto.EvidenceItemDto(
                                "Fortschritt", "62 %", "kpi.progressPercent")),
                        List.of("fact-kpis"))),
                List.of(),
                List.of(),
                true,
                "Disclaimer");
    }
}
