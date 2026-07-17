package com.cgi.kpi.dashboard.ai.client;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
import com.cgi.kpi.dashboard.kpi.dto.ApprovedPortfolioContextDto.CandidateProjectDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto.ApprovedProjectFactDto;

/**
 * Deterministic local mock for CI/dev without external model keys.
 */
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockAiModelClient implements AiModelClient {

    private static final String DISCLAIMER =
            "KI-generierte Einschätzung auf Basis freigegebener Projektdaten. Ersetzt keine Fachentscheidung.";

    @Override
    public ProjectAiAnalysisResponseDto analyze(ApprovedProjectContextDto context) {
        List<ApprovedProjectFactDto> facts = context.facts();
        Optional<ApprovedProjectFactDto> status = find(facts, "project.status");
        Optional<ApprovedProjectFactDto> progress = find(facts, "kpi.progressPercent");
        Optional<ApprovedProjectFactDto> deviation = find(facts, "kpi.scheduleDeviationDays");
        Optional<ApprovedProjectFactDto> budgetDev = find(facts, "budget.forecastDeviation");
        Optional<ApprovedProjectFactDto> risks = find(facts, "kpi.risks.openCount");
        Optional<ApprovedProjectFactDto> problems = find(facts, "kpi.problems.openCount");

        List<PriorityDto> priorities = new ArrayList<>();
        int rank = 1;
        if (deviation.isPresent() && asInt(deviation.get().value()) > 0) {
            priorities.add(new PriorityDto(
                    rank++,
                    "Terminabweichung adressieren",
                    "Die freigegebene Terminabweichung beträgt " + deviation.get().displayValue() + ".",
                    List.of(deviation.get().factId())));
        }
        if (budgetDev.isPresent() && asDouble(budgetDev.get().value()) > 0) {
            priorities.add(new PriorityDto(
                    rank++,
                    "Budgetabweichung prüfen",
                    "Die freigegebene Budgetabweichung liegt bei " + budgetDev.get().displayValue() + ".",
                    List.of(budgetDev.get().factId())));
        }
        if (risks.isPresent() && asInt(risks.get().value()) > 0) {
            priorities.add(new PriorityDto(
                    rank++,
                    "Offene Risiken steuern",
                    "Es liegen " + risks.get().displayValue() + " offene Risiken vor.",
                    List.of(risks.get().factId())));
        }
        if (problems.isPresent() && asInt(problems.get().value()) > 0) {
            priorities.add(new PriorityDto(
                    rank++,
                    "Offene Probleme priorisieren",
                    "Es liegen " + problems.get().displayValue() + " offene Probleme vor.",
                    List.of(problems.get().factId())));
        }
        if (priorities.isEmpty() && progress.isPresent()) {
            priorities.add(new PriorityDto(
                    1,
                    "Projektstatus beobachten",
                    "Aktueller Fortschritt " + progress.get().displayValue()
                            + ", Status " + status.map(ApprovedProjectFactDto::displayValue).orElse("unbekannt") + ".",
                    List.of(progress.get().factId(), status.map(ApprovedProjectFactDto::factId).orElse("project.status"))));
        }

        List<SuggestedActionDto> actions = new ArrayList<>();
        if (!priorities.isEmpty()) {
            PriorityDto top = priorities.get(0);
            actions.add(new SuggestedActionDto(
                    "Steering-Vorbereitung für: " + top.title(),
                    top.reason(),
                    find(facts, "project.lead").map(ApprovedProjectFactDto::displayValue).orElse("Projektleitung"),
                    null,
                    "KPI",
                    top.evidenceFactIds().isEmpty() ? null : top.evidenceFactIds().get(0),
                    "Transparente Entscheidungsgrundlage für das nächste Steering",
                    top.evidenceFactIds(),
                    true));
        }

        List<String> sources = facts.stream()
                .map(ApprovedProjectFactDto::category)
                .distinct()
                .sorted()
                .toList();

        String summary = "Projekt „" + context.projectName() + "“: Status "
                + status.map(ApprovedProjectFactDto::displayValue).orElse("unbekannt")
                + ", Fortschritt "
                + progress.map(ApprovedProjectFactDto::displayValue).orElse("n/a")
                + ". Die Einschätzung basiert ausschließlich auf freigegebenen Fakten; fehlende Bereiche sind ausgewiesen.";

        List<MissingDataDto> missing = context.missingData().stream()
                .map(item -> new MissingDataDto(item.area(), item.description()))
                .toList();

        return new ProjectAiAnalysisResponseDto(
                context.projectId(),
                context.factsAsOf(),
                Instant.now(),
                "SUCCESS",
                sources,
                summary,
                priorities,
                actions,
                missing,
                true,
                DISCLAIMER);
    }

    @Override
    public ProjectAiQuestionResponseDto answer(ApprovedProjectContextDto context, String question) {
        String q = question == null ? "" : question.toLowerCase(Locale.ROOT);
        List<ApprovedProjectFactDto> facts = context.facts();

        Optional<ApprovedProjectFactDto> match = Optional.empty();
        if (q.contains("budget") || q.contains("kosten")) {
            match = find(facts, "budget.forecastDeviation").or(() -> find(facts, "budget.actual"));
        } else if (q.contains("termin") || q.contains("verzöger") || q.contains("meilenstein")) {
            match = find(facts, "kpi.scheduleDeviationDays")
                    .or(() -> facts.stream().filter(f -> f.factId().startsWith("milestone.")).findFirst());
        } else if (q.contains("risiko")) {
            match = find(facts, "kpi.risks.openCount");
        } else if (q.contains("problem")) {
            match = find(facts, "kpi.problems.openCount");
        } else if (q.contains("fortschritt") || q.contains("status")) {
            match = find(facts, "kpi.progressPercent").or(() -> find(facts, "project.status"));
        } else {
            match = facts.stream()
                    .filter(fact -> q.contains(fact.label().toLowerCase(Locale.ROOT)))
                    .findFirst();
        }

        if (match.isEmpty()) {
            return new ProjectAiQuestionResponseDto(
                    "Dazu liegen keine ausreichend konkreten freigegebenen Projektdaten vor.",
                    List.of(),
                    context.factsAsOf(),
                    Instant.now(),
                    true,
                    true,
                    DISCLAIMER);
        }

        ApprovedProjectFactDto fact = match.get();
        return new ProjectAiQuestionResponseDto(
                "Laut freigegebenen Projektdaten: " + fact.label() + " = " + fact.displayValue() + ".",
                List.of(fact.factId()),
                context.factsAsOf(),
                Instant.now(),
                false,
                true,
                DISCLAIMER);
    }

    @Override
    public PortfolioTrendAnalysisResponseDto analyzePortfolio(ApprovedPortfolioContextDto context) {
        List<CandidateProjectDto> ranked = context.candidateProjects().stream()
                .sorted((a, b) -> Integer.compare(urgencyScore(b), urgencyScore(a)))
                .limit(3)
                .toList();

        List<TopProjectDto> top = ranked.stream()
                .map(project -> new TopProjectDto(
                        project.projectId(),
                        project.projectName(),
                        buildReason(project),
                        project.evidenceFactIds()))
                .toList();

        String text = context.facts().isEmpty()
                ? "Für das Portfolio liegen derzeit keine freigegebenen aggregierten Kennzahlen vor."
                : "Auf Basis der freigegebenen Portfolio-KPIs stehen "
                        + top.size()
                        + " Projekte im Fokus. Die Einschätzung interpretiert nur vorhandene Fakten und berechnet keine neuen KPIs.";

        return new PortfolioTrendAnalysisResponseDto(text, true, DISCLAIMER, Instant.now(), top);
    }

    private static int urgencyScore(CandidateProjectDto project) {
        int score = 0;
        if ("CRITICAL".equalsIgnoreCase(project.status())) {
            score += 100;
        } else if ("AT_RISK".equalsIgnoreCase(project.status())) {
            score += 60;
        }
        if (project.scheduleDeviationDays() != null && project.scheduleDeviationDays() > 0) {
            score += Math.min(40, project.scheduleDeviationDays());
        }
        if (project.budgetDeviationPercent() != null && project.budgetDeviationPercent() > 0) {
            score += Math.min(30, project.budgetDeviationPercent().intValue());
        }
        score += project.criticalIssueCount() * 10;
        score += project.openRiskCount() * 5;
        return score;
    }

    private static String buildReason(CandidateProjectDto project) {
        List<String> parts = new ArrayList<>();
        parts.add("Status " + project.statusLabel());
        if (project.scheduleDeviationDays() != null && project.scheduleDeviationDays() > 0) {
            parts.add("Terminabweichung " + project.scheduleDeviationDays() + " Tage");
        }
        if (project.budgetDeviationPercent() != null && project.budgetDeviationPercent() > 0) {
            parts.add("Budgetabweichung " + project.budgetDeviationPercent() + " %");
        }
        if (project.criticalIssueCount() > 0) {
            parts.add(project.criticalIssueCount() + " kritische Probleme/Risiken");
        }
        return String.join("; ", parts) + ".";
    }

    private static Optional<ApprovedProjectFactDto> find(List<ApprovedProjectFactDto> facts, String factId) {
        return facts.stream().filter(fact -> factId.equals(fact.factId())).findFirst();
    }

    private static int asInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }

    private static double asDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return 0.0;
    }
}
