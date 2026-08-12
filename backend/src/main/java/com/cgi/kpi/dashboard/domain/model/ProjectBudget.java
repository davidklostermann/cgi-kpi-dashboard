package com.cgi.kpi.dashboard.domain.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "project_budgets")
public class ProjectBudget extends UuidEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, unique = true, columnDefinition = "uuid")
    private Project project;

    @Column(name = "planned_budget", nullable = false, precision = 14, scale = 2)
    private BigDecimal plannedBudget;

    @Column(name = "actual_budget", nullable = false, precision = 14, scale = 2)
    private BigDecimal actualBudget;

    @Column(name = "planned_effort_days", nullable = false, precision = 10, scale = 2)
    private BigDecimal plannedEffortDays;

    @Column(name = "actual_effort_days", nullable = false, precision = 10, scale = 2)
    private BigDecimal actualEffortDays;

    @Column(nullable = false, length = 3)
    private String currency = "EUR";

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public BigDecimal getPlannedBudget() {
        return plannedBudget;
    }

    public void setPlannedBudget(BigDecimal plannedBudget) {
        this.plannedBudget = plannedBudget;
    }

    public BigDecimal getActualBudget() {
        return actualBudget;
    }

    public void setActualBudget(BigDecimal actualBudget) {
        this.actualBudget = actualBudget;
    }

    public BigDecimal getPlannedEffortDays() {
        return plannedEffortDays;
    }

    public void setPlannedEffortDays(BigDecimal plannedEffortDays) {
        this.plannedEffortDays = plannedEffortDays;
    }

    public BigDecimal getActualEffortDays() {
        return actualEffortDays;
    }

    public void setActualEffortDays(BigDecimal actualEffortDays) {
        this.actualEffortDays = actualEffortDays;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
