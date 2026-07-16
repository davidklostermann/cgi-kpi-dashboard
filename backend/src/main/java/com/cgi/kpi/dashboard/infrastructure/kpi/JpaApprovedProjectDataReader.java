package com.cgi.kpi.dashboard.infrastructure.kpi;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto.ApprovedProjectFactDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto.MissingDataItemDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectInsightsDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectKpiDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectMasterDataDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectPhasesDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectTrendsDto;
import com.cgi.kpi.dashboard.kpi.reader.ApprovedProjectDataReader;
import com.cgi.kpi.dashboard.kpi.reader.ProjectKpiReader;

/**
 * Assembles approved AI facts from existing kpi.* DTOs only (AD-2).
 */
@Component
public class JpaApprovedProjectDataReader implements ApprovedProjectDataReader {

    private final ProjectKpiReader projectKpiReader;

    public JpaApprovedProjectDataReader(ProjectKpiReader projectKpiReader) {
        this.projectKpiReader = projectKpiReader;
    }

    @Override
    public Optional<ApprovedProjectContextDto> readApprovedContext(UUID projectId) {
        Optional<ProjectMasterDataDto> masterOpt = projectKpiReader.readProjectMasterData(projectId);
        if (masterOpt.isEmpty()) {
            return Optional.empty();
        }

        ProjectMasterDataDto master = masterOpt.get();
        ProjectKpiDto kpis = projectKpiReader.readProjectKpis(projectId).orElse(null);
        ProjectInsightsDto insights = projectKpiReader.readProjectInsights(projectId).orElse(null);
        ProjectPhasesDto phases = projectKpiReader.readProjectPhases(projectId).orElse(null);
        ProjectTrendsDto trends = projectKpiReader.readProjectTrends(projectId).orElse(null);

        List<ApprovedProjectFactDto> facts = new ArrayList<>();
        List<MissingDataItemDto> missing = new ArrayList<>();

        Instant factsAsOf = master.lastDataUpdate() != null ? master.lastDataUpdate() : Instant.parse("2026-07-01T08:00:00Z");

        facts.add(fact(
                "project.status",
                "KPI",
                "Ampelstatus",
                master.status(),
                master.statusLabel(),
                "PROJECT",
                master.id().toString(),
                "fact-master"));

        if (kpis != null) {
            facts.add(fact(
                    "kpi.progressPercent",
                    "KPI",
                    "Fortschritt",
                    kpis.progressPercent(),
                    kpis.progressPercent() + " %",
                    "PROJECT_KPI",
                    null,
                    "fact-kpis"));
            facts.add(fact(
                    "kpi.scheduleDeviationDays",
                    "KPI",
                    "Terminabweichung",
                    kpis.schedule().deviationDays(),
                    formatDays(kpis.schedule().deviationDays()),
                    "PROJECT_KPI",
                    null,
                    "fact-kpis"));
            if (kpis.budget().planned() != null) {
                facts.add(fact(
                        "budget.planned",
                        "BUDGET",
                        "Budget Plan",
                        kpis.budget().planned(),
                        formatMoney(kpis.budget().planned()),
                        "PROJECT_BUDGET",
                        null,
                        "fact-budget"));
            }
            if (kpis.budget().actual() != null) {
                facts.add(fact(
                        "budget.actual",
                        "BUDGET",
                        "Budget Ist",
                        kpis.budget().actual(),
                        formatMoney(kpis.budget().actual()),
                        "PROJECT_BUDGET",
                        null,
                        "fact-budget"));
            }
            if (kpis.budget().deviationPercent() != null) {
                facts.add(fact(
                        "budget.forecastDeviation",
                        "BUDGET",
                        "Budgetabweichung",
                        kpis.budget().deviationPercent(),
                        kpis.budget().deviationPercent() + " %",
                        "PROJECT_BUDGET",
                        null,
                        "fact-budget"));
            }
            if (kpis.budget().forecastAtCompletion() != null) {
                facts.add(fact(
                        "budget.forecastAtCompletion",
                        "BUDGET",
                        "Budget-Hochrechnung",
                        kpis.budget().forecastAtCompletion(),
                        formatMoney(kpis.budget().forecastAtCompletion()),
                        "PROJECT_BUDGET",
                        null,
                        "fact-budget"));
            }
            facts.add(fact(
                    "kpi.risks.openCount",
                    "RISK",
                    "Offene Risiken",
                    kpis.risks().openCount(),
                    String.valueOf(kpis.risks().openCount()),
                    "PROJECT_KPI",
                    null,
                    "fact-kpis"));
            facts.add(fact(
                    "kpi.problems.openCount",
                    "PROBLEM",
                    "Offene Probleme",
                    kpis.problems().openCount(),
                    String.valueOf(kpis.problems().openCount()),
                    "PROJECT_KPI",
                    null,
                    "fact-kpis"));
        } else {
            missing.add(new MissingDataItemDto("KPI", "Management-KPIs sind für dieses Projekt nicht verfügbar."));
        }

        if (insights != null && insights.insights() != null) {
            for (var insight : insights.insights()) {
                facts.add(fact(
                        "insight." + insight.code(),
                        "INSIGHT",
                        insight.statement(),
                        insight.code(),
                        insight.rationale(),
                        "PROJECT_INSIGHT",
                        insight.code(),
                        "fact-insights"));
            }
        }

        if (trends != null && trends.comparisonAvailable()) {
            facts.add(fact(
                    "report.progressDeltaPercent",
                    "REPORT_DELTA",
                    "Fortschritt Δ seit letztem Berichtsstand",
                    trends.progressDeltaPercent(),
                    String.valueOf(trends.progressDeltaPercent()),
                    "PROJECT_REPORT_SNAPSHOT",
                    null,
                    "fact-report-comparison"));
            facts.add(fact(
                    "report.statusChange",
                    "REPORT_DELTA",
                    "Ampelstatus-Verlauf",
                    trends.previousStatus() + "→" + trends.currentStatus(),
                    trends.previousStatusLabel() + " → " + trends.currentStatusLabel(),
                    "PROJECT_REPORT_SNAPSHOT",
                    null,
                    "fact-report-comparison"));
        } else {
            missing.add(new MissingDataItemDto(
                    "REPORT_DELTA",
                    "Kein vorheriger Berichtsstand für einen Vergleich vorhanden."));
        }

        if (phases != null) {
            for (var milestone : phases.milestones()) {
                facts.add(fact(
                        "milestone." + sanitize(milestone.name()),
                        "MILESTONE",
                        milestone.name(),
                        milestone.status(),
                        milestone.statusLabel() + " (Plan: " + milestone.plannedDueDate() + ")",
                        "MILESTONE",
                        null,
                        "fact-phases"));
            }
            for (var phase : phases.phases()) {
                facts.add(fact(
                        "phase." + sanitize(phase.name()),
                        "PHASE",
                        phase.name(),
                        phase.status(),
                        phase.statusLabel(),
                        "PROJECT_PHASE",
                        null,
                        "fact-phases"));
            }
        }

        missing.add(new MissingDataItemDto(
                "ACTION",
                "Maßnahmenliste ist noch nicht als freigegebene API verfügbar (Epic 7)."));
        missing.add(new MissingDataItemDto(
                "CAPACITY",
                "Rollen-/Kapazitätsdaten sind im aktuellen Datenmodell nicht freigegeben."));
        missing.add(new MissingDataItemDto(
                "QUALITY",
                "Defect-, Testabdeckungs- und Abnahmedaten fehlen."));

        if (master.projectLead() != null) {
            facts.add(fact(
                    "project.lead",
                    "KPI",
                    "Projektleitung",
                    master.projectLead(),
                    master.projectLead(),
                    "PROJECT",
                    master.id().toString(),
                    "fact-master"));
        }

        return Optional.of(new ApprovedProjectContextDto(
                master.id(),
                master.name(),
                factsAsOf,
                List.copyOf(facts),
                List.copyOf(missing)));
    }

    private static ApprovedProjectFactDto fact(
            String factId,
            String category,
            String label,
            Object value,
            String displayValue,
            String sourceEntityType,
            String sourceEntityId,
            String detailAnchor) {
        return new ApprovedProjectFactDto(
                factId, category, label, value, displayValue, sourceEntityType, sourceEntityId, detailAnchor);
    }

    private static String formatDays(Integer days) {
        if (days == null) {
            return "nicht verfügbar";
        }
        if (days > 0) {
            return "+" + days + " Tage";
        }
        return days + " Tage";
    }

    private static String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "nicht verfügbar";
        }
        return amount.toPlainString() + " €";
    }

    private static String sanitize(String value) {
        return value == null ? "unknown" : value.toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }
}
