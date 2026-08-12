package com.cgi.kpi.dashboard.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cgi.kpi.dashboard.domain.model.ProjectSprint;

public interface ProjectSprintRepository extends JpaRepository<ProjectSprint, UUID> {

    List<ProjectSprint> findByProject_IdOrderBySequenceNoAsc(UUID projectId);

    long countByProject_Id(UUID projectId);
}
