package com.cgi.kpi.dashboard.api.projects;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cgi.kpi.dashboard.api.projects.dto.ProjectDetailDto;
import com.cgi.kpi.dashboard.api.projects.dto.ProjectListItemDto;
import com.cgi.kpi.dashboard.application.ProjectQueryService;
import com.cgi.kpi.dashboard.kpi.dto.ProjectKpiDto;
import com.cgi.kpi.dashboard.kpi.service.ProjectKpiService;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectQueryService projectQueryService;
    private final ProjectKpiService projectKpiService;

    public ProjectController(ProjectQueryService projectQueryService, ProjectKpiService projectKpiService) {
        this.projectQueryService = projectQueryService;
        this.projectKpiService = projectKpiService;
    }

    @GetMapping
    public List<ProjectListItemDto> listProjects() {
        return projectQueryService.listProjects();
    }

    @GetMapping("/{id}")
    public ProjectDetailDto getProject(@PathVariable UUID id) {
        return projectQueryService.getProject(id);
    }

    @GetMapping("/{id}/kpis")
    public ProjectKpiDto getProjectKpis(@PathVariable UUID id) {
        return projectKpiService.getProjectKpis(id);
    }
}
