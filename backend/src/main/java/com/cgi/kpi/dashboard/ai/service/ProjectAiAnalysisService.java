package com.cgi.kpi.dashboard.ai.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.cgi.kpi.dashboard.ai.cache.ProjectAiAnalysisCache;
import com.cgi.kpi.dashboard.ai.client.AiModelClient;
import com.cgi.kpi.dashboard.ai.config.AiProperties;
import com.cgi.kpi.dashboard.ai.config.AiProviderConfigVersionProvider;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiQuestionRequestDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiQuestionResponseDto;
import com.cgi.kpi.dashboard.ai.validation.AiEvidenceValidator;
import com.cgi.kpi.dashboard.api.error.ApiException;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto;
import com.cgi.kpi.dashboard.kpi.reader.ApprovedProjectDataReader;
import com.cgi.kpi.dashboard.security.user.CurrentUserService;
import com.cgi.kpi.dashboard.ai.config.AiActiveConfigProvider;

@Service
public class ProjectAiAnalysisService {

    static final int MAX_QUESTION_LENGTH = 2000;

    private final ApprovedProjectDataReader approvedProjectDataReader;
    private final AiModelClient aiModelClient;
    private final AiEvidenceValidator aiEvidenceValidator;
    private final AiProperties aiProperties;
    private final CurrentUserService currentUserService;
    private final ProjectAiAnalysisCache cache;
    private final AiProviderConfigVersionProvider configVersionProvider;
    private final AiActiveConfigProvider aiActiveConfigProvider;

    public ProjectAiAnalysisService(
            ApprovedProjectDataReader approvedProjectDataReader,
            AiModelClient aiModelClient,
            AiEvidenceValidator aiEvidenceValidator,
            AiProperties aiProperties,
            CurrentUserService currentUserService,
            ProjectAiAnalysisCache cache,
            AiProviderConfigVersionProvider configVersionProvider,
            AiActiveConfigProvider aiActiveConfigProvider) {
        this.approvedProjectDataReader = approvedProjectDataReader;
        this.aiModelClient = aiModelClient;
        this.aiEvidenceValidator = aiEvidenceValidator;
        this.aiProperties = aiProperties;
        this.currentUserService = currentUserService;
        this.cache = cache;
        this.configVersionProvider = configVersionProvider;
        this.aiActiveConfigProvider = aiActiveConfigProvider;
    }

    public void assertReady() {
        ensureEnabled(currentUserService.requireUserId());
    }

    public ProjectAiAnalysisResponseDto analyze(UUID projectId, boolean refresh) {
        // currentUserService.requireAdmin(); // Role check is handled by controller security
        UUID userId = currentUserService.requireUserId();
        ensureEnabled(userId);
        UUID workspaceId = currentUserService.requireWorkspaceId();
        ApprovedProjectContextDto context = loadContext(projectId);
        String cacheKey = ProjectAiAnalysisCache.buildKey(
                userId,
                workspaceId,
                projectId,
                context.factsAsOf(),
                configVersionProvider.currentVersion());
        if (!refresh) {
            ProjectAiAnalysisResponseDto cached = cache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }
        try {
            ProjectAiAnalysisResponseDto raw = aiModelClient.analyze(context);
            ProjectAiAnalysisResponseDto validated = aiEvidenceValidator.validateAnalysis(context, raw);
            cache.put(cacheKey, validated);
            return validated;
        } catch (com.cgi.kpi.dashboard.ai.client.GeminiTransportException ex) {
            throw AiProviderExceptionMapper.toApiException(
                    ex, "Der Projekt-Assistent ist derzeit nicht verfügbar.");
        } catch (IllegalStateException ex) {
            throw AiProviderExceptionMapper.toApiException(
                    ex, "Der Projekt-Assistent ist derzeit nicht verfügbar.");
        }
    }

    public ProjectAiQuestionResponseDto ask(UUID projectId, ProjectAiQuestionRequestDto request) {
        // currentUserService.requireAdmin(); // Role check is handled by controller security
        UUID userId = currentUserService.requireUserId();
        ensureEnabled(userId);
        if (request == null || request.question() == null || request.question().isBlank()) {
            throw new ApiException("BAD_REQUEST", "Question must not be empty", HttpStatus.BAD_REQUEST);
        }
        String question = request.question().trim();
        if (question.length() > MAX_QUESTION_LENGTH) {
            throw new ApiException(
                    "BAD_REQUEST",
                    "Question exceeds maximum length of " + MAX_QUESTION_LENGTH + " characters",
                    HttpStatus.BAD_REQUEST);
        }
        ApprovedProjectContextDto context = loadContext(projectId);
        try {
            ProjectAiQuestionResponseDto raw = aiModelClient.answer(context, question);
            return aiEvidenceValidator.validateQuestion(context, raw);
        } catch (com.cgi.kpi.dashboard.ai.client.GeminiTransportException ex) {
            throw AiProviderExceptionMapper.toApiException(
                    ex, "Der Projekt-Assistent ist derzeit nicht verfügbar.");
        } catch (IllegalStateException ex) {
            throw AiProviderExceptionMapper.toApiException(
                    ex, "Der Projekt-Assistent ist derzeit nicht verfügbar.");
        }
    }

    private ApprovedProjectContextDto loadContext(UUID projectId) {
        return approvedProjectDataReader.readApprovedContext(projectId)
                .orElseThrow(() -> new ApiException("NOT_FOUND", "Project not found", HttpStatus.NOT_FOUND));
    }

    private void ensureEnabled(UUID userId) {
        // Project AI always requires a user-owned Gemini key from KI-Einstellungen,
        // independent of the runtime provider (mock vs gemini).
        if (aiActiveConfigProvider.getActiveApiKey("gemini") == null) {
            throw AiProviderExceptionMapper.toApiException(new ApiException(
                    "AI_KEY_MISSING",
                    "Für Ihren Benutzer ist noch kein KI-API-Key hinterlegt.",
                    HttpStatus.FORBIDDEN));
        }

        String provider = aiProperties.getProvider();
        if ("gemini".equalsIgnoreCase(provider)) {
            if (!aiActiveConfigProvider.isEnabled(provider)) {
                throw new ApiException(
                        "AI_DISABLED",
                        "Projekt-Assistent ist deaktiviert.",
                        HttpStatus.SERVICE_UNAVAILABLE);
            }
            return;
        }
        if (!aiActiveConfigProvider.isEnabled(provider)) {
            throw new ApiException(
                    "AI_DISABLED",
                    "Projekt-Assistent ist deaktiviert.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
