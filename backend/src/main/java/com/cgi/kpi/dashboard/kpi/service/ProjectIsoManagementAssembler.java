package com.cgi.kpi.dashboard.kpi.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.domain.model.IsoBenefitsStatus;
import com.cgi.kpi.dashboard.domain.model.IsoImpactLevel;
import com.cgi.kpi.dashboard.domain.model.IsoScopeTrend;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectIsoManagement;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIsoManagementDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIsoManagementDto.BenefitsCardDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIsoManagementDto.ChangeRequestsCardDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIsoManagementDto.QualityCardDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIsoManagementDto.ScopeCardDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIsoManagementDto.StakeholdersCardDto;

/**
 * Assembles ISO management DTOs for the project detail page (Epic 15).
 */
@Component
public class ProjectIsoManagementAssembler {

    private static final String EMPTY_REASON =
            "Keine ISO-Steuerungsfelder für dieses Projekt hinterlegt.";

    public ProjectIsoManagementDto assembleEmpty(Project project) {
        Instant factsAsOf = project.getLastDataUpdate() != null
                ? project.getLastDataUpdate()
                : Instant.parse("2026-07-01T08:00:00Z");
        return new ProjectIsoManagementDto(
                project.getId(),
                false,
                EMPTY_REASON,
                factsAsOf,
                null,
                null,
                null,
                null,
                null);
    }

    public ProjectIsoManagementDto assemble(Project project, ProjectIsoManagement iso) {
        Instant factsAsOf = iso.getFactsAsOf() != null
                ? iso.getFactsAsOf()
                : (project.getLastDataUpdate() != null
                        ? project.getLastDataUpdate()
                        : Instant.parse("2026-07-01T08:00:00Z"));

        return new ProjectIsoManagementDto(
                project.getId(),
                true,
                null,
                factsAsOf,
                toBenefits(iso),
                toScope(iso),
                toChangeRequests(iso),
                toQuality(iso),
                toStakeholders(iso));
    }

    private static BenefitsCardDto toBenefits(ProjectIsoManagement iso) {
        IsoBenefitsStatus status = iso.getBenefitsStatus();
        return new BenefitsCardDto(
                iso.getExpectedBenefit(),
                iso.getBenefitUnit(),
                iso.getRealizedPercent(),
                status.name(),
                benefitsStatusLabel(status));
    }

    private static ScopeCardDto toScope(ProjectIsoManagement iso) {
        List<String> deviations = new ArrayList<>(2);
        addDeviation(deviations, iso.getDeviation1());
        addDeviation(deviations, iso.getDeviation2());
        IsoScopeTrend trend = iso.getScopeTrend();
        return new ScopeCardDto(
                iso.getScopeStatus(),
                List.copyOf(deviations),
                trend.name(),
                scopeTrendLabel(trend));
    }

    private static void addDeviation(List<String> deviations, String value) {
        if (deviations.size() >= 2) {
            return;
        }
        if (value != null && !value.isBlank()) {
            deviations.add(value.trim());
        }
    }

    private static ChangeRequestsCardDto toChangeRequests(ProjectIsoManagement iso) {
        return new ChangeRequestsCardDto(
                iso.getCrTotal(),
                iso.getCrOpen(),
                iso.getCrInReview(),
                iso.getCrApproved(),
                iso.getImpactSchedule().name(),
                impactLabel(iso.getImpactSchedule()),
                iso.getImpactCost().name(),
                impactLabel(iso.getImpactCost()),
                iso.getImpactScope().name(),
                impactLabel(iso.getImpactScope()));
    }

    private static QualityCardDto toQuality(ProjectIsoManagement iso) {
        return new QualityCardDto(
                iso.getQualityStatus(),
                iso.getOpenDefects(),
                iso.getCriticalDefects(),
                iso.getTestAcceptanceStatus(),
                iso.getQualityProgressPercent());
    }

    private static StakeholdersCardDto toStakeholders(ProjectIsoManagement iso) {
        return new StakeholdersCardDto(
                iso.getSponsorCustomer(),
                iso.getStakeholderStatus(),
                iso.getEscalationStatus(),
                iso.getLastSteeringDate());
    }

    static String benefitsStatusLabel(IsoBenefitsStatus status) {
        return switch (status) {
            case GREEN -> "Auf Kurs";
            case AMBER -> "Beobachten";
            case RED -> "Kritisch";
        };
    }

    static String scopeTrendLabel(IsoScopeTrend trend) {
        return switch (trend) {
            case IMPROVING -> "Verbessernd";
            case STABLE -> "Stabil";
            case DETERIORATING -> "Verschlechternd";
        };
    }

    static String impactLabel(IsoImpactLevel level) {
        return switch (level) {
            case NONE -> "Keine";
            case LOW -> "Gering";
            case MEDIUM -> "Mittel";
            case HIGH -> "Hoch";
        };
    }
}
