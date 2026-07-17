package com.cgi.kpi.dashboard.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "project_capacity_summaries")
public class ProjectCapacitySummary {

    @Id
    @Column(name = "project_id", columnDefinition = "uuid")
    private java.util.UUID projectId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "missing_fte", nullable = false, precision = 6, scale = 2)
    private BigDecimal missingFte;

    @Column(name = "next_availability_date")
    private LocalDate nextAvailabilityDate;

    @Column(name = "overloaded_roles", nullable = false)
    private int overloadedRoles;

    @Column(name = "external_options", nullable = false)
    private int externalOptions;

    @Column(name = "impact_headline", nullable = false, length = 200)
    private String impactHeadline;

    @Column(name = "impact_detail", nullable = false, length = 1000)
    private String impactDetail;

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

    public BigDecimal getMissingFte() {
        return missingFte;
    }

    public void setMissingFte(BigDecimal missingFte) {
        this.missingFte = missingFte;
    }

    public LocalDate getNextAvailabilityDate() {
        return nextAvailabilityDate;
    }

    public void setNextAvailabilityDate(LocalDate nextAvailabilityDate) {
        this.nextAvailabilityDate = nextAvailabilityDate;
    }

    public int getOverloadedRoles() {
        return overloadedRoles;
    }

    public void setOverloadedRoles(int overloadedRoles) {
        this.overloadedRoles = overloadedRoles;
    }

    public int getExternalOptions() {
        return externalOptions;
    }

    public void setExternalOptions(int externalOptions) {
        this.externalOptions = externalOptions;
    }

    public String getImpactHeadline() {
        return impactHeadline;
    }

    public void setImpactHeadline(String impactHeadline) {
        this.impactHeadline = impactHeadline;
    }

    public String getImpactDetail() {
        return impactDetail;
    }

    public void setImpactDetail(String impactDetail) {
        this.impactDetail = impactDetail;
    }

    public Instant getFactsAsOf() {
        return factsAsOf;
    }

    public void setFactsAsOf(Instant factsAsOf) {
        this.factsAsOf = factsAsOf;
    }
}
