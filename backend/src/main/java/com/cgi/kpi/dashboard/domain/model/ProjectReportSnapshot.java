package com.cgi.kpi.dashboard.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Lightweight report snapshot for trend comparison (FR-21 MVP).
 */
@Entity
@Table(name = "project_report_snapshots")
public class ProjectReportSnapshot extends UuidEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, columnDefinition = "uuid")
    private Project project;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "progress_percent", nullable = false)
    private int progressPercent;

    @Column(name = "actual_budget", nullable = false, precision = 14, scale = 2)
    private BigDecimal actualBudget;

    @Column(name = "schedule_deviation_days")
    private Integer scheduleDeviationDays;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "open_risk_count", nullable = false)
    private int openRiskCount;

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(LocalDate snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
    }

    public BigDecimal getActualBudget() {
        return actualBudget;
    }

    public void setActualBudget(BigDecimal actualBudget) {
        this.actualBudget = actualBudget;
    }

    public Integer getScheduleDeviationDays() {
        return scheduleDeviationDays;
    }

    public void setScheduleDeviationDays(Integer scheduleDeviationDays) {
        this.scheduleDeviationDays = scheduleDeviationDays;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getOpenRiskCount() {
        return openRiskCount;
    }

    public void setOpenRiskCount(int openRiskCount) {
        this.openRiskCount = openRiskCount;
    }
}
