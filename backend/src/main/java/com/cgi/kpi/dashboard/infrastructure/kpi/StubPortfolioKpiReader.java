package com.cgi.kpi.dashboard.infrastructure.kpi;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiSummaryDto;
import com.cgi.kpi.dashboard.kpi.reader.PortfolioKpiReader;

/**
 * Placeholder adapter until Epic 4 computes KPIs from mock portfolio data.
 */
@Component
public class StubPortfolioKpiReader implements PortfolioKpiReader {

    @Override
    public PortfolioKpiSummaryDto readPortfolioSummary() {
        return new PortfolioKpiSummaryDto(0, 0.0, 0.0, 0.0, 0);
    }
}
