package com.cgi.kpi.dashboard.kpi.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * ISO 21502 supplementary management fields for the project detail page (FR-33).
 */
public record ProjectIsoManagementDto(
        UUID projectId,
        boolean dataAvailable,
        String emptyReason,
        Instant factsAsOf,
        BenefitsCardDto benefits,
        ScopeCardDto scope,
        ChangeRequestsCardDto changeRequests,
        QualityCardDto quality,
        StakeholdersCardDto stakeholders) {

    public record BenefitsCardDto(
            String expectedBenefit,
            String benefitUnit,
            int realizedPercent,
            String status,
            String statusLabel) {
    }

    public record ScopeCardDto(
            String scopeStatus,
            List<String> deviations,
            String trend,
            String trendLabel) {
    }

    public record ChangeRequestsCardDto(
            int total,
            int open,
            int inReview,
            int approved,
            String impactSchedule,
            String impactScheduleLabel,
            String impactCost,
            String impactCostLabel,
            String impactScope,
            String impactScopeLabel) {
    }

    public record QualityCardDto(
            String qualityStatus,
            int openDefects,
            int criticalDefects,
            String testAcceptanceStatus,
            int progressPercent) {
    }

    public record StakeholdersCardDto(
            String sponsorCustomer,
            String stakeholderStatus,
            String escalationStatus,
            LocalDate lastSteeringDate) {
    }
}
