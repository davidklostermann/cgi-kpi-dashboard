package com.cgi.kpi.dashboard.api.projects;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiQuestionRequestDto;
import com.cgi.kpi.dashboard.ai.dto.ProjectAiQuestionResponseDto;
import com.cgi.kpi.dashboard.ai.service.ProjectAiAnalysisService;

@RestController
@RequestMapping("/api/projects/{projectId}/ai")
public class ProjectAiController {

    private final ProjectAiAnalysisService projectAiAnalysisService;

    public ProjectAiController(ProjectAiAnalysisService projectAiAnalysisService) {
        this.projectAiAnalysisService = projectAiAnalysisService;
    }

    @GetMapping("/analysis")
    public ProjectAiAnalysisResponseDto getAnalysis(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "false") boolean refresh) {
        return projectAiAnalysisService.analyze(projectId, refresh);
    }

    @PostMapping("/questions")
    public ProjectAiQuestionResponseDto askQuestion(
            @PathVariable UUID projectId,
            @RequestBody ProjectAiQuestionRequestDto request) {
        return projectAiAnalysisService.ask(projectId, request);
    }
}
