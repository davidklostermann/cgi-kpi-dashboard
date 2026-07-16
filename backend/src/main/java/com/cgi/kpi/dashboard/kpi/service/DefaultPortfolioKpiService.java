package com.cgi.kpi.dashboard.kpi.service;

import org.springframework.stereotype.Service;

import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterOptionsDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiSummaryDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTableDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTimelineDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTrendDto;
import com.cgi.kpi.dashboard.kpi.reader.PortfolioKpiReader;
import com.cgi.kpi.dashboard.kpi.reader.PortfolioTableReader;
import com.cgi.kpi.dashboard.kpi.reader.PortfolioTimelineReader;
import com.cgi.kpi.dashboard.kpi.reader.PortfolioTrendReader;

/**
 * Delegates portfolio KPI access to the reader layer backed by {@link PortfolioKpiCalculator}.
 */
@Service
public class DefaultPortfolioKpiService implements PortfolioKpiService {

    private final PortfolioKpiReader portfolioKpiReader;
    private final PortfolioTimelineReader portfolioTimelineReader;
    private final PortfolioTableReader portfolioTableReader;
    private final PortfolioTrendReader portfolioTrendReader;

    public DefaultPortfolioKpiService(
            PortfolioKpiReader portfolioKpiReader,
            PortfolioTimelineReader portfolioTimelineReader,
            PortfolioTableReader portfolioTableReader,
            PortfolioTrendReader portfolioTrendReader) {
        this.portfolioKpiReader = portfolioKpiReader;
        this.portfolioTimelineReader = portfolioTimelineReader;
        this.portfolioTableReader = portfolioTableReader;
        this.portfolioTrendReader = portfolioTrendReader;
    }

    @Override
    public PortfolioKpiSummaryDto getPortfolioSummary(PortfolioFilterCriteria criteria) {
        return portfolioKpiReader.readPortfolioSummary(criteria);
    }

    @Override
    public PortfolioFilterOptionsDto getFilterOptions() {
        return portfolioKpiReader.readFilterOptions();
    }

    @Override
    public PortfolioTimelineDto getPortfolioTimeline(PortfolioFilterCriteria criteria) {
        return portfolioTimelineReader.readTimeline(criteria);
    }

    @Override
    public PortfolioTableDto getPortfolioTable(PortfolioFilterCriteria criteria) {
        return portfolioTableReader.readTable(criteria);
    }

    @Override
    public PortfolioTrendDto getPortfolioTrends(PortfolioFilterCriteria criteria) {
        return portfolioTrendReader.readTrends(criteria);
    }
}
