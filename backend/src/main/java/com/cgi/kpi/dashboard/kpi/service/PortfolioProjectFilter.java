package com.cgi.kpi.dashboard.kpi.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiProjectInput;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiRiskInput;

/**
 * Applies FR-8 portfolio filters before KPI aggregation (AD-3).
 */
@Component
public class PortfolioProjectFilter {

    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String RISK_STATUS_OPEN = "OPEN";

    public List<PortfolioKpiProjectInput> applyProjects(
            List<PortfolioKpiProjectInput> projects,
            PortfolioFilterCriteria criteria,
            Set<UUID> projectIdsWithMatchingRiskSeverity) {
        if (projects == null) {
            return List.of();
        }

        PortfolioFilterCriteria.LifecycleFilter lifecycle = criteria.lifecycle() != null
                ? criteria.lifecycle()
                : PortfolioFilterCriteria.LifecycleFilter.ACTIVE;

        return projects.stream()
                .filter(project -> matchesLifecycle(project, lifecycle))
                .filter(project -> matchesCustomer(project, criteria.customer()))
                .filter(project -> matchesProjectLead(project, criteria.projectLead()))
                .filter(project -> matchesStatuses(project, criteria.statuses()))
                .filter(project -> matchesPhase(project, criteria.phase()))
                .filter(project -> matchesReportMonth(project, criteria.reportMonth()))
                .filter(project -> matchesRiskSeverity(project, criteria.riskSeverity(), projectIdsWithMatchingRiskSeverity))
                .toList();
    }

    public Set<UUID> projectIdsMatchingRiskSeverity(
            List<PortfolioKpiRiskInput> risks,
            String riskSeverity) {
        if (riskSeverity == null || riskSeverity.isBlank() || risks == null) {
            return Set.of();
        }
        String normalized = riskSeverity.trim().toUpperCase(Locale.ROOT);
        return risks.stream()
                .filter(risk -> RISK_STATUS_OPEN.equals(risk.status()))
                .filter(risk -> normalized.equals(risk.severity()))
                .map(PortfolioKpiRiskInput::projectId)
                .collect(Collectors.toSet());
    }

    public List<PortfolioKpiRiskInput> risksForProjects(
            List<PortfolioKpiRiskInput> risks,
            List<PortfolioKpiProjectInput> projects) {
        if (risks == null || projects == null || projects.isEmpty()) {
            return List.of();
        }
        Set<UUID> projectIds = projects.stream()
                .map(PortfolioKpiProjectInput::projectId)
                .collect(Collectors.toSet());
        return risks.stream()
                .filter(risk -> projectIds.contains(risk.projectId()))
                .toList();
    }

    private static boolean matchesLifecycle(PortfolioKpiProjectInput project, PortfolioFilterCriteria.LifecycleFilter lifecycle) {
        return switch (lifecycle) {
            case ACTIVE -> !STATUS_COMPLETED.equals(project.status());
            case COMPLETED -> STATUS_COMPLETED.equals(project.status());
            case ALL -> true;
        };
    }

    private static boolean matchesCustomer(PortfolioKpiProjectInput project, String customer) {
        if (customer == null || customer.isBlank()) {
            return true;
        }
        return project.customerName() != null
                && project.customerName().toLowerCase(Locale.ROOT).contains(customer.trim().toLowerCase(Locale.ROOT));
    }

    private static boolean matchesProjectLead(PortfolioKpiProjectInput project, String projectLead) {
        if (projectLead == null || projectLead.isBlank()) {
            return true;
        }
        return project.projectLead() != null
                && project.projectLead().toLowerCase(Locale.ROOT).contains(projectLead.trim().toLowerCase(Locale.ROOT));
    }

    private static boolean matchesStatuses(PortfolioKpiProjectInput project, List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return true;
        }
        return statuses.stream()
                .map(status -> status.trim().toUpperCase(Locale.ROOT))
                .anyMatch(status -> status.equals(project.status()));
    }

    private static boolean matchesPhase(PortfolioKpiProjectInput project, String phase) {
        if (phase == null || phase.isBlank()) {
            return true;
        }
        return project.currentPhaseName() != null
                && project.currentPhaseName().equalsIgnoreCase(phase.trim());
    }

    private static boolean matchesReportMonth(PortfolioKpiProjectInput project, String reportMonth) {
        if (reportMonth == null || reportMonth.isBlank()) {
            return true;
        }
        return project.dataReportMonth() != null
                && reportMonth.trim().equals(project.dataReportMonth().toString());
    }

    private static boolean matchesRiskSeverity(
            PortfolioKpiProjectInput project,
            String riskSeverity,
            Set<UUID> projectIdsWithMatchingRiskSeverity) {
        if (riskSeverity == null || riskSeverity.isBlank()) {
            return true;
        }
        return projectIdsWithMatchingRiskSeverity.contains(project.projectId());
    }
}
