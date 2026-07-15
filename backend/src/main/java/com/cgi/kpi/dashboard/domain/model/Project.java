package com.cgi.kpi.dashboard.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "projects")
public class Project extends UuidEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "planned_end_date", nullable = false)
    private LocalDate plannedEndDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Column(name = "progress_percent", nullable = false)
    private int progressPercent;

    @Column(name = "schedule_deviation_days")
    private Integer scheduleDeviationDays;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "project_lead", length = 200)
    private String projectLead;

    @Column(name = "last_data_update")
    private Instant lastDataUpdate;

    @Column(name = "predicted_end_date")
    private LocalDate predictedEndDate;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProjectPhase> phases = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Milestone> milestones = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Risk> risks = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Problem> problems = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProjectReportSnapshot> reportSnapshots = new ArrayList<>();

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ProjectBudget budget;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getPlannedEndDate() {
        return plannedEndDate;
    }

    public void setPlannedEndDate(LocalDate plannedEndDate) {
        this.plannedEndDate = plannedEndDate;
    }

    public LocalDate getActualEndDate() {
        return actualEndDate;
    }

    public void setActualEndDate(LocalDate actualEndDate) {
        this.actualEndDate = actualEndDate;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
    }

    public Integer getScheduleDeviationDays() {
        return scheduleDeviationDays;
    }

    public void setScheduleDeviationDays(Integer scheduleDeviationDays) {
        this.scheduleDeviationDays = scheduleDeviationDays;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getProjectLead() {
        return projectLead;
    }

    public void setProjectLead(String projectLead) {
        this.projectLead = projectLead;
    }

    public Instant getLastDataUpdate() {
        return lastDataUpdate;
    }

    public void setLastDataUpdate(Instant lastDataUpdate) {
        this.lastDataUpdate = lastDataUpdate;
    }

    public LocalDate getPredictedEndDate() {
        return predictedEndDate;
    }

    public void setPredictedEndDate(LocalDate predictedEndDate) {
        this.predictedEndDate = predictedEndDate;
    }

    public List<ProjectPhase> getPhases() {
        return phases;
    }

    public List<Milestone> getMilestones() {
        return milestones;
    }

    public List<Risk> getRisks() {
        return risks;
    }

    public List<Problem> getProblems() {
        return problems;
    }

    public List<ProjectReportSnapshot> getReportSnapshots() {
        return reportSnapshots;
    }

    public ProjectBudget getBudget() {
        return budget;
    }

    public void setBudget(ProjectBudget budget) {
        this.budget = budget;
        if (budget != null) {
            budget.setProject(this);
        }
    }

    public void addPhase(ProjectPhase phase) {
        phases.add(phase);
        phase.setProject(this);
    }

    public void addMilestone(Milestone milestone) {
        milestones.add(milestone);
        milestone.setProject(this);
    }

    public void addRisk(Risk risk) {
        risks.add(risk);
        risk.setProject(this);
    }

    public void addProblem(Problem problem) {
        problems.add(problem);
        problem.setProject(this);
    }

    public void addReportSnapshot(ProjectReportSnapshot snapshot) {
        reportSnapshots.add(snapshot);
        snapshot.setProject(this);
    }
}
