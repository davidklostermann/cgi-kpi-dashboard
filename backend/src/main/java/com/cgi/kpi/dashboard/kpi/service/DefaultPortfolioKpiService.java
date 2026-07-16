package com.cgi.kpi.dashboard.kpi.service;

import org.springframework.stereotype.Service;

import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterOptionsDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiSummaryDto;
import com.cgi.kpi.dashboard.kpi.reader.PortfolioKpiReader;

/**
 * Delegates portfolio KPI access to the reader layer backed by {@link PortfolioKpiCalculator}.
 */
@Service
public class DefaultPortfolioKpiService implements PortfolioKpiService {

    private final PortfolioKpiReader portfolioKpiReader;

    public DefaultPortfolioKpiService(PortfolioKpiReader portfolioKpiReader) {
        this.portfolioKpiReader = portfolioKpiReader;
    }

    @Override
    public PortfolioKpiSummaryDto getPortfolioSummary(PortfolioFilterCriteria criteria) {
        return portfolioKpiReader.readPortfolioSummary(criteria);
    }

    @Override
    public PortfolioFilterOptionsDto getFilterOptions() {
        return portfolioKpiReader.readFilterOptions();
    }
}
