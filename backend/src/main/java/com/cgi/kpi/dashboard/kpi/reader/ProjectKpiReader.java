package com.cgi.kpi.dashboard.kpi.reader;

import java.util.Optional;
import java.util.UUID;

import com.cgi.kpi.dashboard.kpi.dto.ProjectKpiDto;

/** Reads calculated project KPIs (AD-3). */
public interface ProjectKpiReader {

    Optional<ProjectKpiDto> readProjectKpis(UUID projectId);
}
