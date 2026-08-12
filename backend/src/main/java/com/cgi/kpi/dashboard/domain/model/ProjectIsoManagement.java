package com.cgi.kpi.dashboard.domain.model;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "project_iso_management")
public class ProjectIsoManagement {

    @Id
    @Column(name = "project_id", columnDefinition = "uuid")
    private java.util.UUID projectId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "expected_benefit", nullable = false, length = 500)
    private String expectedBenefit;

    @Column(name = "benefit_unit", nullable = false, length = 80)
    private String benefitUnit;

    @Column(name = "realized_percent", nullable = false)
    private int realizedPercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "benefits_status", nullable = false, length = 10)
    private IsoBenefitsStatus benefitsStatus;

    @Column(name = "scope_status", nullable = false, length = 120)
    private String scopeStatus;

    @Column(name = "deviation_1", length = 300)
    private String deviation1;

    @Column(name = "deviation_2", length = 300)
    private String deviation2;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_trend", nullable = false, length = 20)
    private IsoScopeTrend scopeTrend;

    @Column(name = "cr_total", nullable = false)
    private int crTotal;

    @Column(name = "cr_open", nullable = false)
    private int crOpen;

    @Column(name = "cr_in_review", nullable = false)
    private int crInReview;

    @Column(name = "cr_approved", nullable = false)
    private int crApproved;

    @Enumerated(EnumType.STRING)
    @Column(name = "impact_schedule", nullable = false, length = 10)
    private IsoImpactLevel impactSchedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "impact_cost", nullable = false, length = 10)
    private IsoImpactLevel impactCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "impact_scope", nullable = false, length = 10)
    private IsoImpactLevel impactScope;

    @Column(name = "quality_status", nullable = false, length = 120)
    private String qualityStatus;

    @Column(name = "open_defects", nullable = false)
    private int openDefects;

    @Column(name = "critical_defects", nullable = false)
    private int criticalDefects;

    @Column(name = "test_acceptance_status", nullable = false, length = 200)
    private String testAcceptanceStatus;

    @Column(name = "quality_progress_percent", nullable = false)
    private int qualityProgressPercent;

    @Column(name = "sponsor_customer", nullable = false, length = 200)
    private String sponsorCustomer;

    @Column(name = "stakeholder_status", nullable = false, length = 120)
    private String stakeholderStatus;

    @Column(name = "escalation_status", nullable = false, length = 120)
    private String escalationStatus;

    @Column(name = "last_steering_date")
    private LocalDate lastSteeringDate;

    @Column(name = "facts_as_of", nullable = false)
    private Instant factsAsOf;

    public java.util.UUID getProjectId() {
        return projectId;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
        if (project != null) {
            this.projectId = project.getId();
        }
    }

    public String getExpectedBenefit() {
        return expectedBenefit;
    }

    public void setExpectedBenefit(String expectedBenefit) {
        this.expectedBenefit = expectedBenefit;
    }

    public String getBenefitUnit() {
        return benefitUnit;
    }

    public void setBenefitUnit(String benefitUnit) {
        this.benefitUnit = benefitUnit;
    }

    public int getRealizedPercent() {
        return realizedPercent;
    }

    public void setRealizedPercent(int realizedPercent) {
        this.realizedPercent = realizedPercent;
    }

    public IsoBenefitsStatus getBenefitsStatus() {
        return benefitsStatus;
    }

    public void setBenefitsStatus(IsoBenefitsStatus benefitsStatus) {
        this.benefitsStatus = benefitsStatus;
    }

    public String getScopeStatus() {
        return scopeStatus;
    }

    public void setScopeStatus(String scopeStatus) {
        this.scopeStatus = scopeStatus;
    }

    public String getDeviation1() {
        return deviation1;
    }

    public void setDeviation1(String deviation1) {
        this.deviation1 = deviation1;
    }

    public String getDeviation2() {
        return deviation2;
    }

    public void setDeviation2(String deviation2) {
        this.deviation2 = deviation2;
    }

    public IsoScopeTrend getScopeTrend() {
        return scopeTrend;
    }

    public void setScopeTrend(IsoScopeTrend scopeTrend) {
        this.scopeTrend = scopeTrend;
    }

    public int getCrTotal() {
        return crTotal;
    }

    public void setCrTotal(int crTotal) {
        this.crTotal = crTotal;
    }

    public int getCrOpen() {
        return crOpen;
    }

    public void setCrOpen(int crOpen) {
        this.crOpen = crOpen;
    }

    public int getCrInReview() {
        return crInReview;
    }

    public void setCrInReview(int crInReview) {
        this.crInReview = crInReview;
    }

    public int getCrApproved() {
        return crApproved;
    }

    public void setCrApproved(int crApproved) {
        this.crApproved = crApproved;
    }

    public IsoImpactLevel getImpactSchedule() {
        return impactSchedule;
    }

    public void setImpactSchedule(IsoImpactLevel impactSchedule) {
        this.impactSchedule = impactSchedule;
    }

    public IsoImpactLevel getImpactCost() {
        return impactCost;
    }

    public void setImpactCost(IsoImpactLevel impactCost) {
        this.impactCost = impactCost;
    }

    public IsoImpactLevel getImpactScope() {
        return impactScope;
    }

    public void setImpactScope(IsoImpactLevel impactScope) {
        this.impactScope = impactScope;
    }

    public String getQualityStatus() {
        return qualityStatus;
    }

    public void setQualityStatus(String qualityStatus) {
        this.qualityStatus = qualityStatus;
    }

    public int getOpenDefects() {
        return openDefects;
    }

    public void setOpenDefects(int openDefects) {
        this.openDefects = openDefects;
    }

    public int getCriticalDefects() {
        return criticalDefects;
    }

    public void setCriticalDefects(int criticalDefects) {
        this.criticalDefects = criticalDefects;
    }

    public String getTestAcceptanceStatus() {
        return testAcceptanceStatus;
    }

    public void setTestAcceptanceStatus(String testAcceptanceStatus) {
        this.testAcceptanceStatus = testAcceptanceStatus;
    }

    public int getQualityProgressPercent() {
        return qualityProgressPercent;
    }

    public void setQualityProgressPercent(int qualityProgressPercent) {
        this.qualityProgressPercent = qualityProgressPercent;
    }

    public String getSponsorCustomer() {
        return sponsorCustomer;
    }

    public void setSponsorCustomer(String sponsorCustomer) {
        this.sponsorCustomer = sponsorCustomer;
    }

    public String getStakeholderStatus() {
        return stakeholderStatus;
    }

    public void setStakeholderStatus(String stakeholderStatus) {
        this.stakeholderStatus = stakeholderStatus;
    }

    public String getEscalationStatus() {
        return escalationStatus;
    }

    public void setEscalationStatus(String escalationStatus) {
        this.escalationStatus = escalationStatus;
    }

    public LocalDate getLastSteeringDate() {
        return lastSteeringDate;
    }

    public void setLastSteeringDate(LocalDate lastSteeringDate) {
        this.lastSteeringDate = lastSteeringDate;
    }

    public Instant getFactsAsOf() {
        return factsAsOf;
    }

    public void setFactsAsOf(Instant factsAsOf) {
        this.factsAsOf = factsAsOf;
    }
}
