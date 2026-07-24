package com.cgi.kpi.dashboard.ai.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.cgi.kpi.dashboard.ai.cache.PortfolioAiAnalysisCache;
import com.cgi.kpi.dashboard.ai.client.AiModelClient;
import com.cgi.kpi.dashboard.ai.client.GeminiTransportException;
import com.cgi.kpi.dashboard.ai.config.AiActiveConfigProvider;
import com.cgi.kpi.dashboard.ai.config.AiProperties;
import com.cgi.kpi.dashboard.ai.config.AiProviderConfigVersionProvider;
import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto;
import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto.EvidenceDto;
import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto.PortfolioInsightDto;
import com.cgi.kpi.dashboard.api.error.ApiException;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedPortfolioContextDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;
import com.cgi.kpi.dashboard.kpi.dto.ProjectReportTrendDto;
import com.cgi.kpi.dashboard.kpi.reader.PortfolioKpiReader;
import com.cgi.kpi.dashboard.kpi.reader.PortfolioReportTrendReader;
import com.cgi.kpi.dashboard.kpi.reader.PortfolioTableReader;
import com.cgi.kpi.dashboard.security.user.CurrentUserService;

@Service
public class PortfolioAiAnalysisService {

    private static final int MAX_INSIGHTS = 5;
    private static final Set<String> ACTIVE_TYPES = Set.of("DETERIORATING_TREND", "REPORTING_PATTERN");
    private static final Set<String> ALLOWED_SOURCE_FIELDS = Set.of(
            "snapshot.scheduleDeviationDays",
            "snapshot.status",
            "snapshot.openRiskCount",
            "snapshot.actualBudget",
            "snapshot.progressPercent");
    private static final Set<String> CONFIDENCE_VALUES = Set.of("HIGH", "MEDIUM", "LOW");
    private static final Set<String> DATA_QUALITY_VALUES = Set.of("COMPLETE", "PARTIAL", "INSUFFICIENT");
    private static final String DISCLAIMER =
            "KI-gestützte Formulierung auf Basis deterministischer Portfolio-Muster. Ersetzt keine Fachentscheidung.";

    private final PortfolioKpiReader portfolioKpiReader;
    private final PortfolioTableReader portfolioTableReader;
    private final PortfolioReportTrendReader portfolioReportTrendReader;
    private final ApprovedPortfolioContextAssembler contextAssembler;
    private final PortfolioPatternDetector patternDetector;
    private final AiModelClient aiModelClient;
    private final AiProperties aiProperties;
    private final CurrentUserService currentUserService;
    private final PortfolioAiAnalysisCache cache;
    private final AiProviderConfigVersionProvider configVersionProvider;
    private final AiActiveConfigProvider aiActiveConfigProvider;

    public PortfolioAiAnalysisService(
            PortfolioKpiReader portfolioKpiReader,
            PortfolioTableReader portfolioTableReader,
            PortfolioReportTrendReader portfolioReportTrendReader,
            ApprovedPortfolioContextAssembler contextAssembler,
            PortfolioPatternDetector patternDetector,
            AiModelClient aiModelClient,
            AiProperties aiProperties,
            CurrentUserService currentUserService,
            PortfolioAiAnalysisCache cache,
            AiProviderConfigVersionProvider configVersionProvider,
            AiActiveConfigProvider aiActiveConfigProvider) {
        this.portfolioKpiReader = portfolioKpiReader;
        this.portfolioTableReader = portfolioTableReader;
        this.portfolioReportTrendReader = portfolioReportTrendReader;
        this.contextAssembler = contextAssembler;
        this.patternDetector = patternDetector;
        this.aiModelClient = aiModelClient;
        this.aiProperties = aiProperties;
        this.currentUserService = currentUserService;
        this.cache = cache;
        this.configVersionProvider = configVersionProvider;
        this.aiActiveConfigProvider = aiActiveConfigProvider;
    }

    public PortfolioTrendAnalysisResponseDto analyzeTrend(PortfolioFilterCriteria criteria) {
        currentUserService.requireAdmin();
        UUID userId = currentUserService.requireUserId();
        ensureEnabled(userId);
        UUID workspaceId = currentUserService.requireWorkspaceId();
        PortfolioFilterCriteria safe = criteria == null ? PortfolioFilterCriteria.empty() : criteria;
        String cacheKey = PortfolioAiAnalysisCache.buildKey(
                userId, workspaceId, safe, configVersionProvider.currentVersion());
        PortfolioTrendAnalysisResponseDto cached = cache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        ApprovedPortfolioContextDto context = contextAssembler.assemble(
                portfolioKpiReader.readPortfolioSummary(safe),
                portfolioTableReader.readTable(safe));

        List<UUID> candidateIds = context.candidateProjects().stream()
                .map(ApprovedPortfolioContextDto.CandidateProjectDto::projectId)
                .toList();
        List<ProjectReportTrendDto> trends = portfolioReportTrendReader.readTrendsForProjects(candidateIds);
        List<PortfolioInsightDto> detected = patternDetector.detect(trends);

        try {
            // LLM may refine wording of detector insights; structure/evidence stay detector-owned.
            PortfolioTrendAnalysisResponseDto raw = aiModelClient.analyzePortfolio(context);
            List<PortfolioInsightDto> merged = mergeInsights(detected, raw.insights());
            PortfolioTrendAnalysisResponseDto validated = validate(
                    context,
                    new PortfolioTrendAnalysisResponseDto(
                            merged,
                            true,
                            raw.disclaimer() == null || raw.disclaimer().isBlank() ? DISCLAIMER : raw.disclaimer(),
                            raw.generatedAt()),
                    true);
            cache.put(cacheKey, validated);
            return validated;
        } catch (GeminiTransportException | IllegalStateException ex) {
            PortfolioTrendAnalysisResponseDto fallback = fallbackOrThrow(context, detected, ex);
            cache.put(cacheKey, fallback);
            return fallback;
        } catch (RuntimeException ex) {
            PortfolioTrendAnalysisResponseDto fallback = fallbackOrThrow(context, detected, ex);
            cache.put(cacheKey, fallback);
            return fallback;
        }
    }

    private PortfolioTrendAnalysisResponseDto fallbackOrThrow(
            ApprovedPortfolioContextDto context,
            List<PortfolioInsightDto> detected,
            RuntimeException ex) {
        if (!detected.isEmpty()) {
            return validate(
                    context,
                    new PortfolioTrendAnalysisResponseDto(
                            detected, false, DISCLAIMER, java.time.Instant.now()),
                    false);
        }
        if (ex instanceof GeminiTransportException geminiEx) {
            throw AiProviderExceptionMapper.toApiException(
                    geminiEx, "Die Portfolio-Musteranalyse ist derzeit nicht verfügbar.");
        }
        if (ex instanceof IllegalStateException illegalState) {
            throw AiProviderExceptionMapper.toApiException(
                    illegalState, "Die Portfolio-Musteranalyse ist derzeit nicht verfügbar.");
        }
        throw ex;
    }

    /**
     * Detector insights are authoritative for structure/evidence. Matching LLM insights may refine
     * wording only. LLM-only types are discarded.
     */
    static List<PortfolioInsightDto> mergeInsights(
            List<PortfolioInsightDto> detected,
            List<PortfolioInsightDto> fromModel) {
        if (detected == null || detected.isEmpty()) {
            return List.of();
        }
        Map<String, PortfolioInsightDto> modelByType = new java.util.HashMap<>();
        if (fromModel != null) {
            for (PortfolioInsightDto insight : fromModel) {
                if (insight == null || insight.type() == null || insight.type().isBlank()) {
                    continue;
                }
                modelByType.putIfAbsent(insight.type(), insight);
            }
        }
        List<PortfolioInsightDto> merged = new ArrayList<>();
        for (PortfolioInsightDto base : detected) {
            PortfolioInsightDto refinement = modelByType.get(base.type());
            if (refinement == null) {
                merged.add(base);
                continue;
            }
            merged.add(new PortfolioInsightDto(
                    base.id(),
                    base.type(),
                    blankTo(base.title(), refinement.title()),
                    blankTo(base.finding(), refinement.finding()),
                    blankTo(base.managementImplication(), refinement.managementImplication()),
                    refinement.recommendedAction() != null && !refinement.recommendedAction().isBlank()
                            ? refinement.recommendedAction()
                            : base.recommendedAction(),
                    base.affectedProjectIds(),
                    base.affectedProjectNames(),
                    base.evidence(),
                    base.confidence(),
                    base.dataQuality(),
                    base.detectedAt()));
        }
        return merged;
    }

    private static String blankTo(String fallback, String preferred) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }
        return fallback;
    }

    private PortfolioTrendAnalysisResponseDto validate(
            ApprovedPortfolioContextDto context,
            PortfolioTrendAnalysisResponseDto raw,
            boolean aiGenerated) {
        Map<UUID, String> projectNames = context.candidateProjects().stream()
                .collect(Collectors.toMap(
                        ApprovedPortfolioContextDto.CandidateProjectDto::projectId,
                        ApprovedPortfolioContextDto.CandidateProjectDto::projectName,
                        (left, right) -> left));
        Set<UUID> knownProjects = projectNames.keySet();

        List<PortfolioInsightDto> insights = new ArrayList<>();
        if (raw.insights() != null) {
            for (PortfolioInsightDto insight : raw.insights()) {
                PortfolioInsightDto validated = validateInsight(insight, knownProjects, projectNames);
                if (validated != null) {
                    insights.add(validated);
                }
                if (insights.size() == MAX_INSIGHTS) {
                    break;
                }
            }
        }

        return new PortfolioTrendAnalysisResponseDto(
                List.copyOf(insights),
                aiGenerated,
                raw.disclaimer(),
                raw.generatedAt());
    }

    private static PortfolioInsightDto validateInsight(
            PortfolioInsightDto insight,
            Set<UUID> knownProjects,
            Map<UUID, String> projectNames) {
        if (insight == null || insight.type() == null || !ACTIVE_TYPES.contains(insight.type())) {
            return null;
        }
        if (insight.title() == null || insight.title().isBlank()
                || insight.finding() == null || insight.finding().isBlank()
                || insight.managementImplication() == null || insight.managementImplication().isBlank()) {
            return null;
        }
        if (insight.evidence() == null || insight.evidence().size() < 2) {
            return null;
        }

        List<UUID> affectedIds = new ArrayList<>();
        if (insight.affectedProjectIds() != null) {
            for (UUID id : insight.affectedProjectIds()) {
                if (id != null && knownProjects.contains(id) && !affectedIds.contains(id)) {
                    affectedIds.add(id);
                }
            }
        }
        if (affectedIds.size() < 2) {
            return null;
        }

        List<String> names = affectedIds.stream()
                .map(id -> projectNames.getOrDefault(id, id.toString()))
                .toList();
        Set<UUID> affectedSet = Set.copyOf(affectedIds);
        List<EvidenceDto> evidence = new ArrayList<>();
        for (EvidenceDto item : insight.evidence()) {
            if (item == null || item.label() == null || item.label().isBlank()
                    || item.value() == null || item.value().isBlank()) {
                continue;
            }
            String source = item.sourceField();
            if (source == null || source.isBlank() || !ALLOWED_SOURCE_FIELDS.contains(source)) {
                continue;
            }
            if (item.projectId() == null || !affectedSet.contains(item.projectId())) {
                continue;
            }
            evidence.add(item);
        }
        if (evidence.size() < 2) {
            return null;
        }

        return new PortfolioInsightDto(
                insight.id() == null || insight.id().isBlank()
                        ? insight.type() + "-" + affectedIds.get(0)
                        : insight.id(),
                insight.type(),
                insight.title(),
                insight.finding(),
                insight.managementImplication(),
                insight.recommendedAction(),
                List.copyOf(affectedIds),
                List.copyOf(names),
                List.copyOf(evidence),
                normalizeConfidence(insight.confidence()),
                normalizeDataQuality(insight.dataQuality()),
                insight.detectedAt() == null ? java.time.Instant.now() : insight.detectedAt());
    }

    private static String normalizeConfidence(String value) {
        if (value == null || value.isBlank()) {
            return "MEDIUM";
        }
        String normalized = value.trim().toUpperCase();
        return CONFIDENCE_VALUES.contains(normalized) ? normalized : "MEDIUM";
    }

    private static String normalizeDataQuality(String value) {
        if (value == null || value.isBlank()) {
            return "PARTIAL";
        }
        String normalized = value.trim().toUpperCase();
        return DATA_QUALITY_VALUES.contains(normalized) ? normalized : "PARTIAL";
    }

    private void ensureEnabled(UUID userId) {
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
                        "Portfolio-Assistent ist deaktiviert.",
                        HttpStatus.SERVICE_UNAVAILABLE);
            }
            return;
        }
        if (!aiActiveConfigProvider.isEnabled(provider)) {
            throw new ApiException(
                    "AI_DISABLED",
                    "Portfolio-Assistent ist deaktiviert.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
