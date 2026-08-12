package com.cgi.kpi.dashboard.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "project_work_items")
public class ProjectWorkItem extends UuidEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, columnDefinition = "uuid")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id", columnDefinition = "uuid")
    private ProjectSprint sprint;

    @Column(name = "external_key", nullable = false, length = 40)
    private String externalKey;

    @Column(nullable = false, length = 300)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private WorkItemType itemType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkItemStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkItemPriority priority;

    @Column(name = "story_points")
    private Integer storyPoints;

    @Column(length = 120)
    private String assignee;

    @Column(name = "is_blocker", nullable = false)
    private boolean blocker;

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public ProjectSprint getSprint() {
        return sprint;
    }

    public void setSprint(ProjectSprint sprint) {
        this.sprint = sprint;
    }

    public String getExternalKey() {
        return externalKey;
    }

    public void setExternalKey(String externalKey) {
        this.externalKey = externalKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public WorkItemType getItemType() {
        return itemType;
    }

    public void setItemType(WorkItemType itemType) {
        this.itemType = itemType;
    }

    public WorkItemStatus getStatus() {
        return status;
    }

    public void setStatus(WorkItemStatus status) {
        this.status = status;
    }

    public WorkItemPriority getPriority() {
        return priority;
    }

    public void setPriority(WorkItemPriority priority) {
        this.priority = priority;
    }

    public Integer getStoryPoints() {
        return storyPoints;
    }

    public void setStoryPoints(Integer storyPoints) {
        this.storyPoints = storyPoints;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public boolean isBlocker() {
        return blocker;
    }

    public void setBlocker(boolean blocker) {
        this.blocker = blocker;
    }
}
