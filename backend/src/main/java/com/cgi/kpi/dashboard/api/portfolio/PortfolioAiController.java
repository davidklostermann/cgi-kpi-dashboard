package com.cgi.kpi.dashboard.api.portfolio;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto;
import com.cgi.kpi.dashboard.ai.service.PortfolioAiAnalysisService;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;

@RestController
@RequestMapping("/api/portfolio/ai")
public class PortfolioAiController {

    private final PortfolioAiAnalysisService portfolioAiAnalysisService;

    public PortfolioAiController(PortfolioAiAnalysisService portfolioAiAnalysisService) {
        this.portfolioAiAnalysisService = portfolioAiAnalysisService;
    }

    @GetMapping("/trend-analysis")
    public PortfolioTrendAnalysisResponseDto getTrendAnalysis(
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String projectLead,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) String phase,
            @RequestParam(required = false) String lifecycle,
            @RequestParam(required = false) String reportMonth,
            @RequestParam(required = false) String riskSeverity) {
        return portfolioAiAnalysisService.analyzeTrend(buildCriteria(
                customer, projectLead, status, phase, lifecycle, reportMonth, riskSeverity));
    }

    private static PortfolioFilterCriteria buildCriteria(
            String customer,
            String projectLead,
            List<String> status,
            String phase,
            String lifecycle,
            String reportMonth,
            String riskSeverity) {
        return new PortfolioFilterCriteria(
                customer,
                projectLead,
                status != null ? status : List.of(),
                phase,
                parseLifecycle(lifecycle),
                reportMonth,
                riskSeverity);
    }

    private static PortfolioFilterCriteria.LifecycleFilter parseLifecycle(String lifecycle) {
        if (lifecycle == null || lifecycle.isBlank()) {
            return PortfolioFilterCriteria.LifecycleFilter.ACTIVE;
        }
        return switch (lifecycle.trim().toUpperCase()) {
            case "COMPLETED" -> PortfolioFilterCriteria.LifecycleFilter.COMPLETED;
            case "ALL" -> PortfolioFilterCriteria.LifecycleFilter.ALL;
            default -> PortfolioFilterCriteria.LifecycleFilter.ACTIVE;
        };
    }
}
