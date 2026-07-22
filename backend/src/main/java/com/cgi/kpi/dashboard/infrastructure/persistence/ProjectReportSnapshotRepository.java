package com.cgi.kpi.dashboard.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cgi.kpi.dashboard.domain.model.ProjectReportSnapshot;

public interface ProjectReportSnapshotRepository extends JpaRepository<ProjectReportSnapshot, UUID> {

    @Query("""
            SELECT s FROM ProjectReportSnapshot s
            JOIN FETCH s.project p
            WHERE p.id IN :projectIds
            """)
    List<ProjectReportSnapshot> findByProjectIdsWithProject(@Param("projectIds") Collection<UUID> projectIds);
}
