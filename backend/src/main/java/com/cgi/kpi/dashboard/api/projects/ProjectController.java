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

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectQueryService projectQueryService;

    public ProjectController(ProjectQueryService projectQueryService) {
        this.projectQueryService = projectQueryService;
    }

    @GetMapping
    public List<ProjectListItemDto> listProjects() {
        return projectQueryService.listProjects();
    }

    @GetMapping("/{id}")
    public ProjectDetailDto getProject(@PathVariable UUID id) {
        return projectQueryService.getProject(id);
    }
}
