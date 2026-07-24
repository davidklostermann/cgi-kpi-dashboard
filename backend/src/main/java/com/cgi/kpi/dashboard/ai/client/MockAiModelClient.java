package com.cgi.kpi.dashboard.ai.client;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto.EvidenceItemDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto.MissingDataDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto.PriorityDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto.SuggestedActionDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiQuestionResponseDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedPortfolioContextDto;
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
        Optional<ApprovedProjectFactDto> budgetActual = find(facts, "budget.actual");
        Optional<ApprovedProjectFactDto> budgetPlanned = find(facts, "budget.planned");
        Optional<ApprovedProjectFactDto> risks = find(facts, "kpi.risks.openCount");
        Optional<ApprovedProjectFactDto> problems = find(facts, "kpi.problems.openCount");

        List<PriorityDto> priorities = new ArrayList<>();
        int rank = 1;
        if (deviation.isPresent() && asInt(deviation.get().value()) > 0 && progress.isPresent()) {
            priorities.add(priority(
                    rank++,
                    "Terminabweichung erfordert Gegensteuerung",
                    "Der Liefertermin gerät unter Druck und gebundenes Budget bleibt länger gebunden.",
                    "Gegensteuerung und Termincommitment im Steering freigeben.",
                    List.of(deviation.get(), progress.get())));
        }
        if (budgetDev.isPresent() && asDouble(budgetDev.get().value()) > 0
                && budgetActual.isPresent() && budgetPlanned.isPresent()) {
            priorities.add(priority(
                    rank++,
                    "Budgetabweichung prüfen",
                    "Zusätzliche Mittelbindung oder Scope-Anpassung wird wahrscheinlicher.",
                    "Forecast freigeben oder Scope/Budget neu festlegen.",
                    List.of(budgetActual.get(), budgetPlanned.get(), budgetDev.get())));
        }
        if (risks.isPresent() && asInt(risks.get().value()) > 0 && problems.isPresent()) {
            priorities.add(priority(
                    rank++,
                    "Offene Risiken und Probleme steuern",
                    "Operative Steuerungslast steigt und kann den Status weiter belasten.",
                    "Prioritäten und Owner für die offenen Items bestätigen.",
                    List.of(risks.get(), problems.get())));
        }
        if (priorities.isEmpty() && progress.isPresent() && status.isPresent()) {
            priorities.add(priority(
                    1,
                    "Projektstatus beobachten",
                    "Derzeit kein akuter Steuerungsbedarf aus den belegten Fakten.",
                    "Keine Sofortentscheidung; nächsten Berichtsstand prüfen.",
                    List.of(progress.get(), status.get())));
        }
        if (priorities.size() > 3) {
            priorities = priorities.subList(0, 3);
        }

        List<SuggestedActionDto> actions = List.of();

        List<String> sources = facts.stream()
                .map(ApprovedProjectFactDto::category)
                .distinct()
                .sorted()
                .toList();

        PriorityDto top = priorities.isEmpty() ? null : priorities.get(0);
        String summary = top == null
                ? "Warum auffällig: In den freigegebenen Daten liegen keine belastbaren Abweichungen vor. "
                        + "Auswirkung: Derzeit kein akuter Steuerungsbedarf. "
                        + "Nötige Entscheidung: Keine Sofortentscheidung; nächsten Berichtsstand prüfen."
                : "Warum auffällig: " + top.title() + ". "
                        + "Auswirkung: " + shorten(top.managementImplication()) + " "
                        + "Nötige Entscheidung: " + top.requiredDecision();

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
                false,
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
                    false,
                    false,
                    DISCLAIMER);
        }

        ApprovedProjectFactDto fact = match.get();
        return new ProjectAiQuestionResponseDto(
                "Laut freigegebenen Projektdaten: " + fact.label() + " = " + fact.displayValue() + ".",
                List.of(fact.factId()),
                context.factsAsOf(),
                Instant.now(),
                false,
                false,
                DISCLAIMER);
    }

    @Override
    public PortfolioTrendAnalysisResponseDto analyzePortfolio(ApprovedPortfolioContextDto context) {
        // Pattern insights are filled by PortfolioPatternDetector in the service layer (step 4b).
        // Mock returns an empty insight list so the DTO contract stays consistent without fabricated patterns.
        return new PortfolioTrendAnalysisResponseDto(
                List.of(),
                false,
                DISCLAIMER,
                Instant.now());
    }

    private static PriorityDto priority(
            int rank,
            String title,
            String implication,
            String decision,
            List<ApprovedProjectFactDto> evidenceFacts) {
        List<EvidenceItemDto> evidence = evidenceFacts.stream()
                .map(fact -> new EvidenceItemDto(fact.label(), fact.displayValue(), fact.factId()))
                .toList();
        List<String> factIds = evidenceFacts.stream().map(ApprovedProjectFactDto::factId).toList();
        return new PriorityDto(rank, title, implication, decision, evidence, factIds);
    }

    private static String shorten(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String trimmed = text.trim();
        int max = 160;
        if (trimmed.length() <= max) {
            return trimmed.endsWith(".") ? trimmed : trimmed + ".";
        }
        return trimmed.substring(0, max).trim() + "…";
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
