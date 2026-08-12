package com.cgi.kpi.dashboard.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cgi.kpi.dashboard.domain.model.ProjectWorkItem;
import com.cgi.kpi.dashboard.domain.model.WorkItemStatus;

public interface ProjectWorkItemRepository extends JpaRepository<ProjectWorkItem, UUID> {

    List<ProjectWorkItem> findByProject_Id(UUID projectId);

    long countByProject_Id(UUID projectId);

    long countByProject_IdAndBlockerTrueAndStatusNot(UUID projectId, WorkItemStatus status);
}
