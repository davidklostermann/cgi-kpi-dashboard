package com.cgi.kpi.dashboard.domain.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "project_sprints")
public class ProjectSprint extends UuidEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, columnDefinition = "uuid")
    private Project project;

    @Column(nullable = false, length = 40)
    private String name;

    @Column(name = "sequence_no", nullable = false)
    private int sequenceNo;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SprintLifecycle lifecycle;

    @Column(name = "story_points_planned", nullable = false)
    private int storyPointsPlanned;

    @Column(name = "story_points_completed", nullable = false)
    private int storyPointsCompleted;

    @Column(name = "carry_over_points", nullable = false)
    private int carryOverPoints;

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSequenceNo() {
        return sequenceNo;
    }

    public void setSequenceNo(int sequenceNo) {
        this.sequenceNo = sequenceNo;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public SprintLifecycle getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(SprintLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    public int getStoryPointsPlanned() {
        return storyPointsPlanned;
    }

    public void setStoryPointsPlanned(int storyPointsPlanned) {
        this.storyPointsPlanned = storyPointsPlanned;
    }

    public int getStoryPointsCompleted() {
        return storyPointsCompleted;
    }

    public void setStoryPointsCompleted(int storyPointsCompleted) {
        this.storyPointsCompleted = storyPointsCompleted;
    }

    public int getCarryOverPoints() {
        return carryOverPoints;
    }

    public void setCarryOverPoints(int carryOverPoints) {
        this.carryOverPoints = carryOverPoints;
    }
}
