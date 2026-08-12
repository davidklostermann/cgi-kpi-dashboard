package com.cgi.kpi.dashboard.domain.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Project problem — separate from {@link Risk} (FR-6).
 */
@Entity
@Table(name = "problems")
public class Problem extends UuidEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, columnDefinition = "uuid")
    private Project project;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, length = 30)
    private String severity;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(length = 200)
    private String responsible;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(length = 2000)
    private String countermeasure;

    @Column(length = 50)
    private String category;

    @Column(name = "metric_1_label", length = 80)
    private String metric1Label;

    @Column(name = "metric_1_value", length = 120)
    private String metric1Value;

    @Column(name = "metric_2_label", length = 80)
    private String metric2Label;

    @Column(name = "metric_2_value", length = 120)
    private String metric2Value;

    @Column(name = "metric_3_label", length = 80)
    private String metric3Label;

    @Column(name = "metric_3_value", length = 120)
    private String metric3Value;

    @Column(name = "metric_4_label", length = 80)
    private String metric4Label;

    @Column(name = "metric_4_value", length = 120)
    private String metric4Value;

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResponsible() {
        return responsible;
    }

    public void setResponsible(String responsible) {
        this.responsible = responsible;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public String getCountermeasure() {
        return countermeasure;
    }

    public void setCountermeasure(String countermeasure) {
        this.countermeasure = countermeasure;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMetric1Label() {
        return metric1Label;
    }

    public void setMetric1Label(String metric1Label) {
        this.metric1Label = metric1Label;
    }

    public String getMetric1Value() {
        return metric1Value;
    }

    public void setMetric1Value(String metric1Value) {
        this.metric1Value = metric1Value;
    }

    public String getMetric2Label() {
        return metric2Label;
    }

    public void setMetric2Label(String metric2Label) {
        this.metric2Label = metric2Label;
    }

    public String getMetric2Value() {
        return metric2Value;
    }

    public void setMetric2Value(String metric2Value) {
        this.metric2Value = metric2Value;
    }

    public String getMetric3Label() {
        return metric3Label;
    }

    public void setMetric3Label(String metric3Label) {
        this.metric3Label = metric3Label;
    }

    public String getMetric3Value() {
        return metric3Value;
    }

    public void setMetric3Value(String metric3Value) {
        this.metric3Value = metric3Value;
    }

    public String getMetric4Label() {
        return metric4Label;
    }

    public void setMetric4Label(String metric4Label) {
        this.metric4Label = metric4Label;
    }

    public String getMetric4Value() {
        return metric4Value;
    }

    public void setMetric4Value(String metric4Value) {
        this.metric4Value = metric4Value;
    }
}
