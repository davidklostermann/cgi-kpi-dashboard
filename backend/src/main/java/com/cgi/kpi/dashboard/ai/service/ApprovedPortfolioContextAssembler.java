package com.cgi.kpi.dashboard.ai.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.kpi.dto.ApprovedPortfolioContextDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedPortfolioContextDto.ApprovedPortfolioFactDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedPortfolioContextDto.CandidateProjectDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiSummaryDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTableDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTableRowDto;

/**
 * Aggregates freigegebene Portfolio-Fakten for AI (AD-2 / Story 8.1) — no KPI recalculation.
 */
@Component
public class ApprovedPortfolioContextAssembler {

    public ApprovedPortfolioContextDto assemble(PortfolioKpiSummaryDto summary, PortfolioTableDto table) {
        List<ApprovedPortfolioFactDto> facts = new ArrayList<>();
        Instant factsAsOf = Instant.parse("2026-07-01T08:00:00Z");

        if (summary != null && !summary.empty()) {
            addFact(facts, "portfolio.activeProjectCount", "KPI", "Aktive Projekte",
                    summary.activeProjectCount(), String.valueOf(summary.activeProjectCount()));
            addFact(facts, "portfolio.averageProgressPercent", "KPI", "Durchschnittlicher Fortschritt",
                    summary.averageProgressPercent(), summary.averageProgressPercent() + " %");
            addFact(facts, "portfolio.budgetDeviationPercent", "KPI", "Budgetabweichung",
                    summary.budgetDeviationPercent(), summary.budgetDeviationPercent() + " %");
            addFact(facts, "portfolio.scheduleCompliancePercent", "KPI", "Termintreue",
                    summary.scheduleCompliancePercent(), summary.scheduleCompliancePercent() + " %");
            addFact(facts, "portfolio.criticalRiskCount", "RISK", "Kritische Risiken (offen)",
                    summary.criticalRiskCount(), String.valueOf(summary.criticalRiskCount()));
            if (summary.statusDistribution() != null) {
                addFact(facts, "portfolio.status.onTrack", "KPI", "Projekte auf Kurs",
                        summary.statusDistribution().onTrack(),
                        String.valueOf(summary.statusDistribution().onTrack()));
                addFact(facts, "portfolio.status.atRisk", "KPI", "Projekte beobachten",
                        summary.statusDistribution().atRisk(),
                        String.valueOf(summary.statusDistribution().atRisk()));
                addFact(facts, "portfolio.status.critical", "KPI", "Projekte kritisch",
                        summary.statusDistribution().critical(),
                        String.valueOf(summary.statusDistribution().critical()));
            }
        }

        List<CandidateProjectDto> candidates = new ArrayList<>();
        if (table != null && table.projects() != null) {
            List<PortfolioTableRowDto> rows = table.projects().stream()
                    .sorted(Comparator
                            .comparingInt((PortfolioTableRowDto row) -> statusRank(row.status()))
                            .reversed()
                            .thenComparing(
                                    (PortfolioTableRowDto row) ->
                                            row.scheduleDeviationDays() == null ? 0 : row.scheduleDeviationDays(),
                                    Comparator.reverseOrder()))
                    .limit(12)
                    .toList();

            for (PortfolioTableRowDto row : rows) {
                if (row.lastDataUpdate() != null && row.lastDataUpdate().isAfter(factsAsOf)) {
                    factsAsOf = row.lastDataUpdate();
                }
                String prefix = "project." + row.id() + ".";
                List<String> evidence = List.of(
                        prefix + "status",
                        prefix + "scheduleDeviationDays",
                        prefix + "budgetDeviationPercent",
                        prefix + "openRiskCount",
                        prefix + "progressPercent",
                        prefix + "criticalIssueCount");
                addFact(facts, prefix + "status", "PROJECT", row.name() + " Status",
                        row.status(), row.statusLabel());
                addFact(facts, prefix + "scheduleDeviationDays", "PROJECT", row.name() + " Terminabweichung",
                        row.scheduleDeviationDays(),
                        row.scheduleDeviationDays() == null ? "n/a" : row.scheduleDeviationDays() + " Tage");
                addFact(facts, prefix + "budgetDeviationPercent", "PROJECT", row.name() + " Budgetabweichung",
                        row.budgetDeviationPercent(),
                        row.budgetDeviationPercent() == null ? "n/a" : row.budgetDeviationPercent() + " %");
                addFact(facts, prefix + "openRiskCount", "PROJECT", row.name() + " offene Risiken",
                        row.openRiskCount(), String.valueOf(row.openRiskCount()));
                addFact(facts, prefix + "progressPercent", "PROJECT", row.name() + " Fortschritt",
                        row.progressPercent(), row.progressPercent() + " %");
                addFact(facts, prefix + "criticalIssueCount", "PROJECT", row.name() + " kritische Issues",
                        row.criticalIssueCount(), String.valueOf(row.criticalIssueCount()));

                candidates.add(new CandidateProjectDto(
                        row.id(),
                        row.name(),
                        row.status(),
                        row.statusLabel(),
                        row.progressPercent(),
                        row.scheduleDeviationDays(),
                        row.budgetDeviationPercent(),
                        row.openRiskCount(),
                        row.criticalIssueCount(),
                        evidence));
            }
        }

        return new ApprovedPortfolioContextDto(factsAsOf, List.copyOf(facts), List.copyOf(candidates));
    }

    private static void addFact(
            List<ApprovedPortfolioFactDto> facts,
            String factId,
            String category,
            String label,
            Object value,
            String display) {
        facts.add(new ApprovedPortfolioFactDto(factId, category, label, value, display));
    }

    private static int statusRank(String status) {
        if (status == null) {
            return 0;
        }
        return switch (status.toUpperCase(Locale.ROOT)) {
            case "CRITICAL" -> 3;
            case "AT_RISK" -> 2;
            case "ON_TRACK" -> 1;
            default -> 0;
        };
    }
}
