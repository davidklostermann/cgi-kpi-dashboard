package com.cgi.kpi.dashboard.ai.client;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto;
import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto.TopProjectDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto.MissingDataDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto.PriorityDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto.SuggestedActionDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiQuestionResponseDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedPortfolioContextDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedPortfolioContextDto.ApprovedPortfolioFactDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedPortfolioContextDto.CandidateProjectDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto.ApprovedProjectFactDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Gemini-backed model client. Never logs prompts or API keys.
 */
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "gemini")
public class GeminiAiModelClient implements AiModelClient {

    private static final String DISCLAIMER =
            "KI-generierte Einschätzung auf Basis freigegebener Backend-Daten. Ersetzt keine Fachentscheidung.";

    private final GeminiApiTransport transport;
    private final ObjectMapper objectMapper;

    public GeminiAiModelClient(GeminiApiTransport transport, ObjectMapper objectMapper) {
        this.transport = transport;
        this.objectMapper = objectMapper;
    }

    @Override
    public ProjectAiAnalysisResponseDto analyze(ApprovedProjectContextDto context) {
        String prompt = """
                Du bist ein Projekt-Assistent. Nutze ausschließlich die folgenden freigegebenen Fakten.
                Erfinde keine Kennzahlen. Antworte nur als JSON mit Feldern:
                summary (string), priorities (array of {rank,title,reason,evidenceFactIds}),
                suggestedActions (array of {title,reason,suggestedOwner,evidenceFactIds,expectedEffect}),
                missingData (array of {area,description}).
                evidenceFactIds müssen aus der Fact-Liste stammen.

                Projekt: %s
                Fakten:
                %s
                """.formatted(context.projectName(), factsAsText(context.facts()));

        JsonNode root = parseJson(transport.generateJson(prompt));
        List<PriorityDto> priorities = new ArrayList<>();
        for (JsonNode node : root.path("priorities")) {
            priorities.add(new PriorityDto(
                    node.path("rank").asInt(priorities.size() + 1),
                    text(node, "title"),
                    text(node, "reason"),
                    stringList(node.path("evidenceFactIds"))));
        }
        List<SuggestedActionDto> actions = new ArrayList<>();
        for (JsonNode node : root.path("suggestedActions")) {
            actions.add(new SuggestedActionDto(
                    text(node, "title"),
                    text(node, "reason"),
                    textOrNull(node, "suggestedOwner"),
                    null,
                    "KPI",
                    null,
                    textOrNull(node, "expectedEffect"),
                    stringList(node.path("evidenceFactIds")),
                    true));
        }
        List<MissingDataDto> missing = new ArrayList<>();
        for (JsonNode node : root.path("missingData")) {
            missing.add(new MissingDataDto(text(node, "area"), text(node, "description")));
        }
        if (missing.isEmpty()) {
            missing = context.missingData().stream()
                    .map(m -> new MissingDataDto(m.area(), m.description()))
                    .toList();
        }

        List<String> sources = context.facts().stream()
                .map(ApprovedProjectFactDto::category)
                .distinct()
                .sorted()
                .toList();

        return new ProjectAiAnalysisResponseDto(
                context.projectId(),
                context.factsAsOf(),
                Instant.now(),
                "SUCCESS",
                sources,
                text(root, "summary"),
                List.copyOf(priorities),
                List.copyOf(actions),
                List.copyOf(missing),
                true,
                DISCLAIMER);
    }

    @Override
    public ProjectAiQuestionResponseDto answer(ApprovedProjectContextDto context, String question) {
        String prompt = """
                Beantworte die Frage nur mit freigegebenen Projektdaten. Antworte als JSON:
                { "answer": string, "evidenceFactIds": string[], "insufficientEvidence": boolean }.
                Wenn die Daten nicht reichen: insufficientEvidence=true und kurze Begründung.

                Frage: %s
                Fakten:
                %s
                """.formatted(question, factsAsText(context.facts()));

        JsonNode root = parseJson(transport.generateJson(prompt));
        boolean insufficient = root.path("insufficientEvidence").asBoolean(false);
        List<String> evidence = stringList(root.path("evidenceFactIds"));
        if (insufficient || evidence.isEmpty()) {
            return new ProjectAiQuestionResponseDto(
                    "Dazu liegen keine ausreichend konkreten freigegebenen Projektdaten vor.",
                    List.of(),
                    context.factsAsOf(),
                    Instant.now(),
                    true,
                    true,
                    DISCLAIMER);
        }
        return new ProjectAiQuestionResponseDto(
                text(root, "answer"),
                evidence,
                context.factsAsOf(),
                Instant.now(),
                false,
                true,
                DISCLAIMER);
    }

    @Override
    public PortfolioTrendAnalysisResponseDto analyzePortfolio(ApprovedPortfolioContextDto context) {
        StringBuilder candidates = new StringBuilder();
        for (CandidateProjectDto project : context.candidateProjects()) {
            candidates.append("- ")
                    .append(project.projectId()).append(" | ")
                    .append(project.projectName()).append(" | status=")
                    .append(project.status()).append(" | progress=")
                    .append(project.progressPercent()).append(" | deviationDays=")
                    .append(project.scheduleDeviationDays()).append(" | budgetDev=")
                    .append(project.budgetDeviationPercent()).append(" | risks=")
                    .append(project.openRiskCount()).append(" | criticalIssues=")
                    .append(project.criticalIssueCount()).append(" | evidence=")
                    .append(project.evidenceFactIds()).append('\n');
        }

        String prompt = """
                Erstelle eine Portfolio-Trendanalyse. Nutze nur die freigegebenen Fakten.
                Erfinde keine KPI-Werte. Antworte als JSON:
                { "text": string, "topProjects": [ { "projectId": uuid, "projectName": string,
                  "reason": string, "evidenceFactIds": string[] } ] }
                Genau bis zu drei topProjects, priorisiert nach Handlungsbedarf.

                Portfolio-Fakten:
                %s
                Kandidaten:
                %s
                """.formatted(portfolioFactsAsText(context.facts()), candidates);

        JsonNode root = parseJson(transport.generateJson(prompt));
        List<TopProjectDto> top = new ArrayList<>();
        for (JsonNode node : root.path("topProjects")) {
            UUID id;
            try {
                id = UUID.fromString(text(node, "projectId"));
            } catch (Exception ex) {
                continue;
            }
            top.add(new TopProjectDto(
                    id,
                    text(node, "projectName"),
                    text(node, "reason"),
                    stringList(node.path("evidenceFactIds"))));
            if (top.size() == 3) {
                break;
            }
        }
        return new PortfolioTrendAnalysisResponseDto(
                text(root, "text"),
                true,
                DISCLAIMER,
                Instant.now(),
                List.copyOf(top));
    }

    private JsonNode parseJson(String raw) {
        try {
            String cleaned = raw.trim();
            if (cleaned.startsWith("```")) {
                int start = cleaned.indexOf('{');
                int end = cleaned.lastIndexOf('}');
                if (start >= 0 && end > start) {
                    cleaned = cleaned.substring(start, end + 1);
                }
            }
            return objectMapper.readTree(cleaned);
        } catch (Exception ex) {
            throw new GeminiTransportException("Gemini lieferte ungültiges JSON.", ex);
        }
    }

    private static String factsAsText(List<ApprovedProjectFactDto> facts) {
        StringBuilder sb = new StringBuilder();
        for (ApprovedProjectFactDto fact : facts) {
            sb.append(fact.factId()).append(" = ").append(fact.displayValue()).append('\n');
        }
        return sb.toString();
    }

    private static String portfolioFactsAsText(List<ApprovedPortfolioFactDto> facts) {
        StringBuilder sb = new StringBuilder();
        for (ApprovedPortfolioFactDto fact : facts) {
            sb.append(fact.factId()).append(" = ").append(fact.displayValue()).append('\n');
        }
        return sb.toString();
    }

    private static List<String> stringList(JsonNode node) {
        List<String> values = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                if (!item.asText().isBlank()) {
                    values.add(item.asText());
                }
            }
        }
        return values;
    }

    private static String text(JsonNode node, String field) {
        String value = node.path(field).asText("");
        return value.isBlank() ? "" : value;
    }

    private static String textOrNull(JsonNode node, String field) {
        String value = node.path(field).asText("");
        return value.isBlank() ? null : value;
    }
}
