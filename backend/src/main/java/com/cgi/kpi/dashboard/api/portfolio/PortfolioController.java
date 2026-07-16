package com.cgi.kpi.dashboard.api.portfolio;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterOptionsDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiSummaryDto;
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
        PortfolioFilterCriteria criteria = new PortfolioFilterCriteria(
                customer,
                projectLead,
                status != null ? status : List.of(),
                phase,
                parseLifecycle(lifecycle),
                reportMonth,
                riskSeverity);
        return portfolioKpiService.getPortfolioSummary(criteria);
    }

    @GetMapping("/filters/options")
    public PortfolioFilterOptionsDto getFilterOptions() {
        return portfolioKpiService.getFilterOptions();
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
