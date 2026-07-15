package com.cgi.kpi.dashboard.kpi.service;

import org.springframework.stereotype.Service;

import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiSummaryDto;
import com.cgi.kpi.dashboard.kpi.reader.PortfolioKpiReader;

/**
 * Delegates portfolio KPI access to the reader layer; placeholder until Epic 4 calculation logic.
 */
@Service
public class DefaultPortfolioKpiService implements PortfolioKpiService {

    private final PortfolioKpiReader portfolioKpiReader;

    public DefaultPortfolioKpiService(PortfolioKpiReader portfolioKpiReader) {
        this.portfolioKpiReader = portfolioKpiReader;
    }

    @Override
    public PortfolioKpiSummaryDto getPortfolioSummary() {
        return portfolioKpiReader.readPortfolioSummary();
    }
}
