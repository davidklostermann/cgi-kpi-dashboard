package com.cgi.kpi.dashboard.ai.client;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto;
import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto.EvidenceDto;
import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto.PortfolioInsightDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto.EvidenceItemDto;
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
                Du bist ein Projekt-Assistent für Führungskräfte. Nutze ausschließlich die freigegebenen Fakten.
                Erfinde keine Kennzahlen. Wiederhole keine sichtbaren KPI-Listen. Überschreibe niemals den Ampelstatus.
                Antworte nur als JSON mit Feldern:
                summary (string, max. 3 Sätze: Managementbewertung mit Warum / wichtigste Auswirkung / nötige Entscheidung),
                priorities (array, max. 3, of {
                  rank, title, managementImplication, requiredDecision,
                  evidence: [{label, value, sourceField}], evidenceFactIds
                }),
                suggestedActions (meist leeres array — nur bei konkreter, belegter Handlung; keine generischen Maßnahmenlisten),
                missingData (array of {area,description}).
                Jede Priorität braucht mindestens 2 verständliche Belege als Klartext (z. B. label="Terminabweichung", value="46 Tage").
                sourceField und evidenceFactIds müssen aus der Fact-Liste stammen. Aussagen ohne Belege weglassen.

                Projekt: %s
                Fakten:
                %s
                """.formatted(context.projectName(), factsAsText(context.facts()));

        JsonNode root = parseJson(transport.generateJson(prompt));
        List<PriorityDto> priorities = new ArrayList<>();
        for (JsonNode node : root.path("priorities")) {
            List<EvidenceItemDto> evidence = new ArrayList<>();
            for (JsonNode ev : node.path("evidence")) {
                evidence.add(new EvidenceItemDto(
                        text(ev, "label"),
                        text(ev, "value"),
                        textOrNull(ev, "sourceField")));
            }
            priorities.add(new PriorityDto(
                    node.path("rank").asInt(priorities.size() + 1),
                    text(node, "title"),
                    text(node, "managementImplication"),
                    text(node, "requiredDecision"),
                    List.copyOf(evidence),
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
                Formuliere ausschließlich projektübergreifende Portfolio-Muster. Erfinde keine Ursachen.
                Nur Typen DETERIORATING_TREND oder REPORTING_PATTERN. Jeder Insight braucht ≥2 Projekte und ≥2 Belege.
                Antworte als JSON:
                { "insights": [ {
                    "id": string, "type": string, "title": string, "finding": string,
                    "managementImplication": string, "recommendedAction": string|null,
                    "affectedProjectIds": uuid[], "affectedProjectNames": string[],
                    "evidence": [{ "label": string, "value": string, "projectId": uuid|null,
                      "reportDate": "YYYY-MM-DD"|null, "sourceField": string|null }],
                    "confidence": "HIGH"|"MEDIUM"|"LOW",
                    "dataQuality": "COMPLETE"|"PARTIAL"|"INSUFFICIENT"
                } ] }
                Wenn keine belastbaren Muster: insights=[].

                Portfolio-Fakten:
                %s
                Kandidaten:
                %s
                """.formatted(portfolioFactsAsText(context.facts()), candidates);

        JsonNode root = parseJson(transport.generateJson(prompt));
        List<PortfolioInsightDto> insights = new ArrayList<>();
        for (JsonNode node : root.path("insights")) {
            List<UUID> projectIds = new ArrayList<>();
            for (JsonNode idNode : node.path("affectedProjectIds")) {
                try {
                    projectIds.add(UUID.fromString(idNode.asText()));
                } catch (Exception ignored) {
                    // skip invalid
                }
            }
            List<EvidenceDto> evidence = new ArrayList<>();
            for (JsonNode ev : node.path("evidence")) {
                UUID projectId = null;
                String projectIdText = textOrNull(ev, "projectId");
                if (projectIdText != null) {
                    try {
                        projectId = UUID.fromString(projectIdText);
                    } catch (Exception ignored) {
                        projectId = null;
                    }
                }
                java.time.LocalDate reportDate = null;
                String reportDateText = textOrNull(ev, "reportDate");
                if (reportDateText != null) {
                    try {
                        reportDate = java.time.LocalDate.parse(reportDateText);
                    } catch (Exception ignored) {
                        reportDate = null;
                    }
                }
                evidence.add(new EvidenceDto(
                        text(ev, "label"),
                        text(ev, "value"),
                        projectId,
                        reportDate,
                        textOrNull(ev, "sourceField")));
            }
            insights.add(new PortfolioInsightDto(
                    text(node, "id"),
                    text(node, "type"),
                    text(node, "title"),
                    text(node, "finding"),
                    text(node, "managementImplication"),
                    textOrNull(node, "recommendedAction"),
                    List.copyOf(projectIds),
                    stringList(node.path("affectedProjectNames")),
                    List.copyOf(evidence),
                    text(node, "confidence"),
                    text(node, "dataQuality"),
                    Instant.now()));
            if (insights.size() == 5) {
                break;
            }
        }
        return new PortfolioTrendAnalysisResponseDto(
                List.copyOf(insights),
                true,
                DISCLAIMER,
                Instant.now());
    }

    private JsonNode parseJson(String raw) {
        try {
            return objectMapper.readTree(extractJsonPayload(raw));
        } catch (Exception ex) {
            throw new GeminiTransportException("Gemini lieferte ungültiges JSON.", ex);
        }
    }

    static String extractJsonPayload(String raw) {
        String cleaned = raw.trim();
        if (!cleaned.startsWith("```")) {
            return cleaned;
        }
        int start = cleaned.indexOf('{');
        if (start < 0) {
            return cleaned;
        }
        int depth = 0;
        for (int index = start; index < cleaned.length(); index++) {
            char current = cleaned.charAt(index);
            if (current == '{') {
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0) {
                    return cleaned.substring(start, index + 1);
                }
            }
        }
        return cleaned.substring(start);
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
