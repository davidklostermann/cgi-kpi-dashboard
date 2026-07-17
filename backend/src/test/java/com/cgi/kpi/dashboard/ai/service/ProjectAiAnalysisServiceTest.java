package com.cgi.kpi.dashboard.ai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.cgi.kpi.dashboard.ai.client.AiModelClient;
import com.cgi.kpi.dashboard.ai.client.GeminiTransportException;
import com.cgi.kpi.dashboard.ai.config.AiProperties;
import com.cgi.kpi.dashboard.ai.validation.AiEvidenceValidator;
import com.cgi.kpi.dashboard.api.error.ApiException;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto.ApprovedProjectFactDto;
import com.cgi.kpi.dashboard.kpi.reader.ApprovedProjectDataReader;

class ProjectAiAnalysisServiceTest {

    private static final UUID PROJECT_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");

    private ApprovedProjectDataReader reader;
    private AiModelClient modelClient;
    private AiProperties properties;
    private ProjectAiAnalysisService service;

    @BeforeEach
    void setUp() {
        reader = mock(ApprovedProjectDataReader.class);
        modelClient = mock(AiModelClient.class);
        properties = new AiProperties();
        properties.setEnabled(true);
        service = new ProjectAiAnalysisService(
                reader, modelClient, new AiEvidenceValidator(), properties);
        when(reader.readApprovedContext(PROJECT_ID)).thenReturn(Optional.of(context()));
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
}
