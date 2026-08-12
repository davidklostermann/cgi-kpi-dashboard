package com.cgi.kpi.dashboard.application;

import java.util.List;
import java.util.UUID;

import com.cgi.kpi.dashboard.api.projects.dto.ProjectDetailDto;
import com.cgi.kpi.dashboard.api.projects.dto.ProjectListItemDto;

/**
 * Read-only project queries for REST API (AD-5).
 */
public interface ProjectQueryService {

    List<ProjectListItemDto> listProjects();

    ProjectDetailDto getProject(UUID projectId);
}
