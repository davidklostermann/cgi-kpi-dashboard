package com.cgi.kpi.dashboard.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cgi.kpi.dashboard.domain.model.ProjectRoleCapacity;

public interface ProjectRoleCapacityRepository extends JpaRepository<ProjectRoleCapacity, UUID> {

    List<ProjectRoleCapacity> findByProject_IdOrderBySortOrderAsc(UUID projectId);
}
