package com.cgi.kpi.dashboard.api.portfolio;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterOptionsDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiSummaryDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTableDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTimelineDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTrendDto;
import com.cgi.kpi.dashboard.kpi.service.PortfolioKpiService;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioKpiService portfolioKpiService;

    public PortfolioController(PortfolioKpiService portfolioKpiService) {
        this.portfolioKpiService = portfolioKpiService;
    }

    @GetMapping("/kpis")
    public PortfolioKpiSummaryDto getPortfolioKpis(
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String projectLead,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) String phase,
            @RequestParam(required = false) String lifecycle,
            @RequestParam(required = false) String reportMonth,
            @RequestParam(required = false) String riskSeverity) {
        return portfolioKpiService.getPortfolioSummary(buildCriteria(
                customer, projectLead, status, phase, lifecycle, reportMonth, riskSeverity));
    }

    @GetMapping("/timeline")
    public PortfolioTimelineDto getPortfolioTimeline(
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String projectLead,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) String phase,
            @RequestParam(required = false) String lifecycle,
            @RequestParam(required = false) String reportMonth,
            @RequestParam(required = false) String riskSeverity) {
        return portfolioKpiService.getPortfolioTimeline(buildCriteria(
                customer, projectLead, status, phase, lifecycle, reportMonth, riskSeverity));
    }

    @GetMapping("/projects")
    public PortfolioTableDto getPortfolioProjects(
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String projectLead,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) String phase,
            @RequestParam(required = false) String lifecycle,
            @RequestParam(required = false) String reportMonth,
            @RequestParam(required = false) String riskSeverity) {
        return portfolioKpiService.getPortfolioTable(buildCriteria(
                customer, projectLead, status, phase, lifecycle, reportMonth, riskSeverity));
    }

    @GetMapping("/trends")
    public PortfolioTrendDto getPortfolioTrends(
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String projectLead,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) String phase,
            @RequestParam(required = false) String lifecycle,
            @RequestParam(required = false) String reportMonth,
            @RequestParam(required = false) String riskSeverity) {
        return portfolioKpiService.getPortfolioTrends(buildCriteria(
                customer, projectLead, status, phase, lifecycle, reportMonth, riskSeverity));
    }

    @GetMapping("/filters/options")
    public PortfolioFilterOptionsDto getFilterOptions() {
        return portfolioKpiService.getFilterOptions();
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
