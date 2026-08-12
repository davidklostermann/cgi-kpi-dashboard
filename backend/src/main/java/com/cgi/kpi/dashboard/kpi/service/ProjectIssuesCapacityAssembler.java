package com.cgi.kpi.dashboard.kpi.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.domain.model.Problem;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectCapacitySummary;
import com.cgi.kpi.dashboard.domain.model.ProjectRoleCapacity;
import com.cgi.kpi.dashboard.domain.model.Risk;
import com.cgi.kpi.dashboard.kpi.dto.ProjectCapacityDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectCapacityDto.CapacitySummaryDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectCapacityDto.RoleCapacityItemDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIssuesActionsDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIssuesActionsDto.IssueActionItemDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIssuesActionsDto.MetricDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIssuesActionsDto.RequiredDecisionDto;

/**
 * Assembles operational issues/actions and capacity DTOs for the project detail page.
 */
@Component
public class ProjectIssuesCapacityAssembler {

    private static final String STATUS_OPEN = "OPEN";
    private static final String FACTS_BADGE = "Fakten aus Backend";
    private static final Pattern DECISION_SIGNAL = Pattern.compile(
            "(?i)(entscheidung|freigeben|steering|eskala)");

    public ProjectIssuesActionsDto assembleIssuesActions(Project project, List<Problem> problems, List<Risk> risks) {
        List<IssueActionItemDto> items = new ArrayList<>();
        for (Problem problem : problems) {
            if (!STATUS_OPEN.equals(problem.getStatus())) {
                continue;
            }
            items.add(fromProblem(problem));
        }
        for (Risk risk : risks) {
            if (!STATUS_OPEN.equals(risk.getStatus())) {
                continue;
            }
            items.add(fromRisk(risk));
        }
        items.sort(Comparator
                .comparingInt((IssueActionItemDto item) -> severityRank(item.severity()))
                .thenComparing(IssueActionItemDto::itemType)
                .thenComparing(IssueActionItemDto::title, String.CASE_INSENSITIVE_ORDER));

        Instant factsAsOf = project.getLastDataUpdate() != null
                ? project.getLastDataUpdate()
                : Instant.parse("2026-07-01T08:00:00Z");

        return new ProjectIssuesActionsDto(project.getId(), FACTS_BADGE, factsAsOf, List.copyOf(items));
    }

    public ProjectCapacityDto assembleCapacity(
            Project project,
            List<ProjectRoleCapacity> roles,
            ProjectCapacitySummary summary) {
        Instant factsAsOf = summary != null && summary.getFactsAsOf() != null
                ? summary.getFactsAsOf()
                : (project.getLastDataUpdate() != null
                        ? project.getLastDataUpdate()
                        : Instant.parse("2026-07-01T08:00:00Z"));

        List<RoleCapacityItemDto> roleItems = roles.stream()
                .sorted(Comparator.comparingInt(ProjectRoleCapacity::getSortOrder))
                .map(role -> new RoleCapacityItemDto(
                        role.getId(),
                        role.getRoleName(),
                        role.getRequiredFte(),
                        role.getAvailableFte(),
                        role.getCoveragePercent()))
                .toList();

        CapacitySummaryDto summaryDto = null;
        if (summary != null) {
            summaryDto = new CapacitySummaryDto(
                    summary.getMissingFte(),
                    summary.getNextAvailabilityDate(),
                    summary.getOverloadedRoles(),
                    summary.getExternalOptions(),
                    summary.getImpactHeadline(),
                    summary.getImpactDetail());
        }

        String badge = "Datenstand "
                + java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")
                        .withZone(java.time.ZoneOffset.UTC)
                        .format(factsAsOf);

        return new ProjectCapacityDto(project.getId(), factsAsOf, badge, roleItems, summaryDto);
    }

    private static IssueActionItemDto fromProblem(Problem problem) {
        List<MetricDto> metricList = metrics(
                problem.getMetric1Label(), problem.getMetric1Value(),
                problem.getMetric2Label(), problem.getMetric2Value(),
                problem.getMetric3Label(), problem.getMetric3Value(),
                problem.getMetric4Label(), problem.getMetric4Value());
        String impact = resolveImpact(metricList);
        Integer overdueDays = overdueDays(problem.getTargetDate());
        String actionText = problem.getCountermeasure();
        boolean escalation = escalationNeeded(problem.getSeverity(), overdueDays, actionText);
        return new IssueActionItemDto(
                problem.getId(),
                "PROBLEM",
                "Problem",
                normalizeCategory(problem.getCategory()),
                problem.getTitle(),
                problem.getDescription(),
                impact,
                problem.getSeverity(),
                severityLabel(problem.getSeverity()),
                severityLabel(problem.getSeverity()),
                metricList,
                problem.getResponsible(),
                problem.getTargetDate(),
                overdueDays,
                overdueLabel(overdueDays),
                actionText,
                escalation,
                "COUNTERMEASURE",
                "Laufende Maßnahme",
                actionText,
                requiredDecision(
                        problem.getResponsible(),
                        problem.getTargetDate(),
                        impact,
                        actionText,
                        problem.getSeverity(),
                        overdueDays));
    }

    private static IssueActionItemDto fromRisk(Risk risk) {
        List<MetricDto> metricList = metrics(
                risk.getMetric1Label(), risk.getMetric1Value(),
                risk.getMetric2Label(), risk.getMetric2Value(),
                risk.getMetric3Label(), risk.getMetric3Value(),
                risk.getMetric4Label(), risk.getMetric4Value());
        String impact = resolveImpact(metricList);
        Integer overdueDays = overdueDays(risk.getDueDate());
        String actionText = risk.getMitigationMeasure();
        boolean escalation = escalationNeeded(risk.getSeverity(), overdueDays, actionText);
        return new IssueActionItemDto(
                risk.getId(),
                "RISK",
                "Risiko",
                normalizeCategory(risk.getCategory()),
                risk.getTitle(),
                risk.getDescription(),
                impact,
                risk.getSeverity(),
                severityLabel(risk.getSeverity()),
                severityLabel(risk.getSeverity()),
                metricList,
                risk.getOwnerName(),
                risk.getDueDate(),
                overdueDays,
                overdueLabel(overdueDays),
                actionText,
                escalation,
                "MITIGATION",
                "Vorbereitung / Gegensteuerung",
                actionText,
                requiredDecision(
                        risk.getOwnerName(),
                        risk.getDueDate(),
                        impact,
                        actionText,
                        risk.getSeverity(),
                        overdueDays));
    }

    private static RequiredDecisionDto requiredDecision(
            String owner,
            LocalDate dueDate,
            String impact,
            String actionText,
            String severity,
            Integer overdueDays) {
        if (!needsDecision(actionText, severity, overdueDays)) {
            return null;
        }
        String impactIfDeferred = impact != null && !impact.isBlank()
                ? "Bei Nichtentscheidung bleibt die Auswirkung bestehen: " + impact
                : "Bei Nichtentscheidung bleibt der Handlungsbedarf ungelöst und kann den Projektverlauf weiter belasten.";
        return new RequiredDecisionDto(
                owner != null && !owner.isBlank() ? owner : "Nicht zugeordnet",
                dueDate,
                impactIfDeferred);
    }

    private static boolean needsDecision(String actionText, String severity, Integer overdueDays) {
        if (actionText != null && DECISION_SIGNAL.matcher(actionText).find()) {
            return true;
        }
        return "CRITICAL".equalsIgnoreCase(severity) && overdueDays != null && overdueDays > 0;
    }

    private static boolean escalationNeeded(String severity, Integer overdueDays, String actionText) {
        if ("CRITICAL".equalsIgnoreCase(severity)) {
            return true;
        }
        if (overdueDays != null && overdueDays > 0) {
            return true;
        }
        return actionText != null && actionText.toLowerCase(Locale.ROOT).contains("eskala");
    }

    private static Integer overdueDays(LocalDate dueDate) {
        if (dueDate == null) {
            return null;
        }
        long days = ChronoUnit.DAYS.between(dueDate, ProjectKpiCalculator.REFERENCE_DATE);
        if (days <= 0) {
            return null;
        }
        return (int) days;
    }

    private static String overdueLabel(Integer overdueDays) {
        if (overdueDays == null || overdueDays <= 0) {
            return null;
        }
        return overdueDays == 1
                ? "Überfällig seit 1 Tag"
                : "Überfällig seit " + overdueDays + " Tagen";
    }

    private static String resolveImpact(List<MetricDto> metricList) {
        for (MetricDto metric : metricList) {
            String label = metric.label().toLowerCase(Locale.ROOT);
            if (label.contains("auswirkung") || label.contains("potenzial") || label.contains("wirkung")) {
                return metric.label() + ": " + metric.value();
            }
        }
        return null;
    }

    private static List<MetricDto> metrics(
            String l1, String v1, String l2, String v2, String l3, String v3, String l4, String v4) {
        List<MetricDto> result = new ArrayList<>(4);
        addMetric(result, l1, v1);
        addMetric(result, l2, v2);
        addMetric(result, l3, v3);
        addMetric(result, l4, v4);
        return List.copyOf(result);
    }

    private static void addMetric(List<MetricDto> target, String label, String value) {
        if (label == null || label.isBlank() || value == null || value.isBlank()) {
            return;
        }
        target.add(new MetricDto(label, value));
    }

    private static String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return "ALLGEMEIN";
        }
        return category.trim().toUpperCase(Locale.ROOT);
    }

    private static String severityLabel(String severity) {
        if (severity == null) {
            return "Unbekannt";
        }
        return switch (severity.toUpperCase(Locale.ROOT)) {
            case "CRITICAL" -> "Kritisch";
            case "HIGH" -> "Hoch";
            case "MEDIUM" -> "Mittel";
            case "LOW" -> "Niedrig";
            default -> severity;
        };
    }

    private static int severityRank(String severity) {
        if (severity == null) {
            return 99;
        }
        return switch (severity.toUpperCase(Locale.ROOT)) {
            case "CRITICAL" -> 0;
            case "HIGH" -> 1;
            case "MEDIUM" -> 2;
            case "LOW" -> 3;
            default -> 50;
        };
    }
}
