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
import com.cgi.kpi.dashboard.kpi.dto.ProjectIsoManagementDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIsoManagementDto.BenefitsCardDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIsoManagementDto.ChangeRequestsCardDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIsoManagementDto.QualityCardDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIsoManagementDto.ScopeCardDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIsoManagementDto.StakeholdersCardDto;
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

        appendIsoManagementFacts(projectId, facts, missing);

        missing.add(new MissingDataItemDto(
                "ACTION",
                "Maßnahmenliste ist noch nicht als freigegebene API verfügbar (Epic 7)."));
        missing.add(new MissingDataItemDto(
                "CAPACITY",
                "Rollen-/Kapazitätsdaten sind im aktuellen Datenmodell nicht freigegeben."));

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

    private void appendIsoManagementFacts(
            UUID projectId,
            List<ApprovedProjectFactDto> facts,
            List<MissingDataItemDto> missing) {
        Optional<ProjectIsoManagementDto> isoOpt = projectKpiReader.readProjectIsoManagement(projectId);
        if (isoOpt.isEmpty() || !isoOpt.get().dataAvailable()) {
            String reason = isoOpt.map(ProjectIsoManagementDto::emptyReason)
                    .filter(text -> text != null && !text.isBlank())
                    .orElse("Keine ISO-Steuerungsfelder für dieses Projekt hinterlegt.");
            missing.add(new MissingDataItemDto("ISO_MANAGEMENT", reason));
            return;
        }

        ProjectIsoManagementDto iso = isoOpt.get();
        BenefitsCardDto benefits = iso.benefits();
        if (benefits != null) {
            facts.add(fact(
                    "iso.benefits.expectedBenefit",
                    "ISO_BENEFITS",
                    "Erwarteter Nutzen",
                    benefits.expectedBenefit(),
                    benefits.expectedBenefit(),
                    "PROJECT_ISO_MANAGEMENT",
                    projectId.toString(),
                    "fact-iso-management"));
            facts.add(fact(
                    "iso.benefits.realizedPercent",
                    "ISO_BENEFITS",
                    "Realisierter Nutzen",
                    benefits.realizedPercent(),
                    benefits.realizedPercent() + " %",
                    "PROJECT_ISO_MANAGEMENT",
                    projectId.toString(),
                    "fact-iso-management"));
            facts.add(fact(
                    "iso.benefits.status",
                    "ISO_BENEFITS",
                    "Nutzen-Status",
                    benefits.status(),
                    benefits.statusLabel(),
                    "PROJECT_ISO_MANAGEMENT",
                    projectId.toString(),
                    "fact-iso-management"));
        }

        ScopeCardDto scope = iso.scope();
        if (scope != null) {
            facts.add(fact(
                    "iso.scope.status",
                    "ISO_SCOPE",
                    "Scope-Status",
                    scope.scopeStatus(),
                    scope.scopeStatus(),
                    "PROJECT_ISO_MANAGEMENT",
                    projectId.toString(),
                    "fact-iso-management"));
            facts.add(fact(
                    "iso.scope.trend",
                    "ISO_SCOPE",
                    "Scope-Trend",
                    scope.trend(),
                    scope.trendLabel(),
                    "PROJECT_ISO_MANAGEMENT",
                    projectId.toString(),
                    "fact-iso-management"));
            if (!scope.deviations().isEmpty()) {
                facts.add(fact(
                        "iso.scope.deviations",
                        "ISO_SCOPE",
                        "Wesentliche Abweichungen",
                        scope.deviations(),
                        String.join("; ", scope.deviations()),
                        "PROJECT_ISO_MANAGEMENT",
                        projectId.toString(),
                        "fact-iso-management"));
            }
        }

        ChangeRequestsCardDto changes = iso.changeRequests();
        if (changes != null) {
            facts.add(fact(
                    "iso.changes.openCount",
                    "ISO_CHANGE",
                    "Offene Change Requests",
                    changes.open(),
                    String.valueOf(changes.open()),
                    "PROJECT_ISO_MANAGEMENT",
                    projectId.toString(),
                    "fact-iso-management"));
            facts.add(fact(
                    "iso.changes.impactSchedule",
                    "ISO_CHANGE",
                    "CR-Impact Termin",
                    changes.impactSchedule(),
                    changes.impactScheduleLabel(),
                    "PROJECT_ISO_MANAGEMENT",
                    projectId.toString(),
                    "fact-iso-management"));
            facts.add(fact(
                    "iso.changes.impactCost",
                    "ISO_CHANGE",
                    "CR-Impact Kosten",
                    changes.impactCost(),
                    changes.impactCostLabel(),
                    "PROJECT_ISO_MANAGEMENT",
                    projectId.toString(),
                    "fact-iso-management"));
        }

        QualityCardDto quality = iso.quality();
        if (quality != null) {
            facts.add(fact(
                    "iso.quality.criticalDefects",
                    "ISO_QUALITY",
                    "Kritische Defects",
                    quality.criticalDefects(),
                    String.valueOf(quality.criticalDefects()),
                    "PROJECT_ISO_MANAGEMENT",
                    projectId.toString(),
                    "fact-iso-management"));
            facts.add(fact(
                    "iso.quality.openDefects",
                    "ISO_QUALITY",
                    "Offene Defects",
                    quality.openDefects(),
                    String.valueOf(quality.openDefects()),
                    "PROJECT_ISO_MANAGEMENT",
                    projectId.toString(),
                    "fact-iso-management"));
            facts.add(fact(
                    "iso.quality.progressPercent",
                    "ISO_QUALITY",
                    "Qualitäts-Fortschritt",
                    quality.progressPercent(),
                    quality.progressPercent() + " %",
                    "PROJECT_ISO_MANAGEMENT",
                    projectId.toString(),
                    "fact-iso-management"));
        }

        StakeholdersCardDto stakeholders = iso.stakeholders();
        if (stakeholders != null) {
            facts.add(fact(
                    "iso.stakeholders.escalationStatus",
                    "ISO_STAKEHOLDER",
                    "Eskalationsstatus",
                    stakeholders.escalationStatus(),
                    stakeholders.escalationStatus(),
                    "PROJECT_ISO_MANAGEMENT",
                    projectId.toString(),
                    "fact-iso-management"));
            if (stakeholders.lastSteeringDate() != null) {
                facts.add(fact(
                        "iso.stakeholders.lastSteeringDate",
                        "ISO_STAKEHOLDER",
                        "Letztes Steering",
                        stakeholders.lastSteeringDate(),
                        stakeholders.lastSteeringDate().toString(),
                        "PROJECT_ISO_MANAGEMENT",
                        projectId.toString(),
                        "fact-iso-management"));
            }
        }
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
