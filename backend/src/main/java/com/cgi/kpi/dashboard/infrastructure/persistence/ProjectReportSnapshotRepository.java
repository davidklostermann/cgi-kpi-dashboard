package com.cgi.kpi.dashboard.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cgi.kpi.dashboard.domain.model.ProjectReportSnapshot;

public interface ProjectReportSnapshotRepository extends JpaRepository<ProjectReportSnapshot, UUID> {
}
