package com.cgi.kpi.dashboard.ai.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.cgi.kpi.dashboard.ai.client.AiModelClient;
import com.cgi.kpi.dashboard.ai.client.GeminiTransportException;
import com.cgi.kpi.dashboard.ai.config.AiProperties;
import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto;
import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto.TopProjectDto;
import com.cgi.kpi.dashboard.api.error.ApiException;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedPortfolioContextDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;
import com.cgi.kpi.dashboard.kpi.reader.PortfolioKpiReader;
import com.cgi.kpi.dashboard.kpi.reader.PortfolioTableReader;

@Service
public class PortfolioAiAnalysisService {

    private final PortfolioKpiReader portfolioKpiReader;
    private final PortfolioTableReader portfolioTableReader;
    private final ApprovedPortfolioContextAssembler contextAssembler;
    private final AiModelClient aiModelClient;
    private final AiProperties aiProperties;

    public PortfolioAiAnalysisService(
            PortfolioKpiReader portfolioKpiReader,
            PortfolioTableReader portfolioTableReader,
            ApprovedPortfolioContextAssembler contextAssembler,
            AiModelClient aiModelClient,
            AiProperties aiProperties) {
        this.portfolioKpiReader = portfolioKpiReader;
        this.portfolioTableReader = portfolioTableReader;
        this.contextAssembler = contextAssembler;
        this.aiModelClient = aiModelClient;
        this.aiProperties = aiProperties;
    }

    public PortfolioTrendAnalysisResponseDto analyzeTrend(PortfolioFilterCriteria criteria) {
        ensureEnabled();
        PortfolioFilterCriteria safe = criteria == null ? PortfolioFilterCriteria.empty() : criteria;
        ApprovedPortfolioContextDto context = contextAssembler.assemble(
                portfolioKpiReader.readPortfolioSummary(safe),
                portfolioTableReader.readTable(safe));

        try {
            PortfolioTrendAnalysisResponseDto raw = aiModelClient.analyzePortfolio(context);
            return validate(context, raw);
        } catch (GeminiTransportException ex) {
            throw AiProviderExceptionMapper.toApiException(
                    ex, "Die KI-Trendanalyse ist derzeit nicht verfügbar.");
        } catch (IllegalStateException ex) {
            throw AiProviderExceptionMapper.toApiException(
                    ex, "Die KI-Trendanalyse ist derzeit nicht verfügbar.");
        }
    }

    private PortfolioTrendAnalysisResponseDto validate(
            ApprovedPortfolioContextDto context,
            PortfolioTrendAnalysisResponseDto raw) {
        Set<String> allowed = context.facts().stream()
                .map(ApprovedPortfolioContextDto.ApprovedPortfolioFactDto::factId)
                .collect(Collectors.toCollection(HashSet::new));
        Set<java.util.UUID> knownProjects = context.candidateProjects().stream()
                .map(ApprovedPortfolioContextDto.CandidateProjectDto::projectId)
                .collect(Collectors.toCollection(HashSet::new));

        List<TopProjectDto> top = new ArrayList<>();
        if (raw.topProjects() != null) {
            for (TopProjectDto project : raw.topProjects()) {
                if (project.projectId() == null || !knownProjects.contains(project.projectId())) {
                    continue;
                }
                List<String> evidence = project.evidenceFactIds() == null
                        ? List.of()
                        : project.evidenceFactIds().stream().filter(allowed::contains).distinct().toList();
                if (evidence.isEmpty()) {
                    continue;
                }
                top.add(new TopProjectDto(
                        project.projectId(),
                        project.projectName(),
                        project.reason(),
                        evidence));
                if (top.size() == 3) {
                    break;
                }
            }
        }

        String text = raw.text() == null || raw.text().isBlank()
                ? "Keine ausreichenden freigegebenen Portfolio-Fakten für eine Trendanalyse."
                : raw.text();

        return new PortfolioTrendAnalysisResponseDto(
                text,
                true,
                raw.disclaimer(),
                raw.generatedAt(),
                List.copyOf(top));
    }

    private void ensureEnabled() {
        if (!aiProperties.isEnabled()) {
            throw new ApiException(
                    "AI_DISABLED",
                    "Portfolio-Assistent ist deaktiviert.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
