package com.cgi.kpi.dashboard.ai.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.cgi.kpi.dashboard.ai.client.AiModelClient;
import com.cgi.kpi.dashboard.ai.config.AiProperties;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiQuestionRequestDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiQuestionResponseDto;
import com.cgi.kpi.dashboard.ai.validation.AiEvidenceValidator;
import com.cgi.kpi.dashboard.api.error.ApiException;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto;
import com.cgi.kpi.dashboard.kpi.reader.ApprovedProjectDataReader;

@Service
public class ProjectAiAnalysisService {

    private final ApprovedProjectDataReader approvedProjectDataReader;
    private final AiModelClient aiModelClient;
    private final AiEvidenceValidator aiEvidenceValidator;
    private final AiProperties aiProperties;
    private final Map<String, ProjectAiAnalysisResponseDto> cache = new ConcurrentHashMap<>();

    public ProjectAiAnalysisService(
            ApprovedProjectDataReader approvedProjectDataReader,
            AiModelClient aiModelClient,
            AiEvidenceValidator aiEvidenceValidator,
            AiProperties aiProperties) {
        this.approvedProjectDataReader = approvedProjectDataReader;
        this.aiModelClient = aiModelClient;
        this.aiEvidenceValidator = aiEvidenceValidator;
        this.aiProperties = aiProperties;
    }

    public ProjectAiAnalysisResponseDto analyze(UUID projectId, boolean refresh) {
        ensureEnabled();
        ApprovedProjectContextDto context = loadContext(projectId);
        String cacheKey = projectId + "|" + context.factsAsOf();
        if (!refresh) {
            ProjectAiAnalysisResponseDto cached = cache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }
        ProjectAiAnalysisResponseDto raw = aiModelClient.analyze(context);
        ProjectAiAnalysisResponseDto validated = aiEvidenceValidator.validateAnalysis(context, raw);
        cache.put(cacheKey, validated);
        return validated;
    }

    public ProjectAiQuestionResponseDto ask(UUID projectId, ProjectAiQuestionRequestDto request) {
        ensureEnabled();
        if (request == null || request.question() == null || request.question().isBlank()) {
            throw new ApiException("BAD_REQUEST", "Question must not be empty", HttpStatus.BAD_REQUEST);
        }
        ApprovedProjectContextDto context = loadContext(projectId);
        ProjectAiQuestionResponseDto raw = aiModelClient.answer(context, request.question().trim());
        return aiEvidenceValidator.validateQuestion(context, raw);
    }

    private ApprovedProjectContextDto loadContext(UUID projectId) {
        return approvedProjectDataReader.readApprovedContext(projectId)
                .orElseThrow(() -> new ApiException("NOT_FOUND", "Project not found", HttpStatus.NOT_FOUND));
    }

    private void ensureEnabled() {
        if (!aiProperties.isEnabled()) {
            throw new ApiException(
                    "AI_DISABLED",
                    "Projekt-Assistent ist deaktiviert.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
