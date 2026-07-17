package com.cgi.kpi.dashboard.domain.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "project_role_capacities")
public class ProjectRoleCapacity extends UuidEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, columnDefinition = "uuid")
    private Project project;

    @Column(name = "role_name", nullable = false, length = 120)
    private String roleName;

    @Column(name = "required_fte", nullable = false, precision = 6, scale = 2)
    private BigDecimal requiredFte;

    @Column(name = "available_fte", nullable = false, precision = 6, scale = 2)
    private BigDecimal availableFte;

    @Column(name = "coverage_percent", nullable = false)
    private int coveragePercent;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public BigDecimal getRequiredFte() {
        return requiredFte;
    }

    public void setRequiredFte(BigDecimal requiredFte) {
        this.requiredFte = requiredFte;
    }

    public BigDecimal getAvailableFte() {
        return availableFte;
    }

    public void setAvailableFte(BigDecimal availableFte) {
        this.availableFte = availableFte;
    }

    public int getCoveragePercent() {
        return coveragePercent;
    }

    public void setCoveragePercent(int coveragePercent) {
        this.coveragePercent = coveragePercent;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
