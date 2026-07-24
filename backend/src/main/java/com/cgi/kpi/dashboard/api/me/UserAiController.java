package com.cgi.kpi.dashboard.api.me;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cgi.kpi.dashboard.ai.service.ProjectAiAnalysisService;
import com.cgi.kpi.dashboard.api.me.dto.AiReadinessResponseDto;

@RestController
@RequestMapping("/api/me/ai")
public class UserAiController {

    private final ProjectAiAnalysisService projectAiAnalysisService;

    public UserAiController(ProjectAiAnalysisService projectAiAnalysisService) {
        this.projectAiAnalysisService = projectAiAnalysisService;
    }

    @GetMapping("/readiness")
    public AiReadinessResponseDto getReadiness() {
        projectAiAnalysisService.assertReady();
        return new AiReadinessResponseDto(true);
    }
}
