import { Injectable } from '@angular/core';

import { ApiClient } from './api-client.service';
import { PortfolioFilterOptions } from '../../shared/models/portfolio-filter.model';
import { PortfolioKpiSummary } from '../../shared/models/portfolio-kpi.model';
import { PortfolioTimeline } from '../../shared/models/portfolio-timeline.model';
import { PortfolioTable } from '../../shared/models/portfolio-table.model';
import { PortfolioTrends } from '../../shared/models/portfolio-trends.model';

/** Portfolio facts API (FR-1, FR-3, FR-8). */
@Injectable({ providedIn: 'root' })
export class PortfolioApiService extends ApiClient {
  getPortfolioKpis(filters: Record<string, string | string[]> = {}) {
    return this.get<PortfolioKpiSummary>('/portfolio/kpis', filters);
  }

  getPortfolioTimeline(filters: Record<string, string | string[]> = {}) {
    return this.get<PortfolioTimeline>('/portfolio/timeline', filters);
  }

  getPortfolioProjects(filters: Record<string, string | string[]> = {}) {
    return this.get<PortfolioTable>('/portfolio/projects', filters);
  }

  getPortfolioTrends(filters: Record<string, string | string[]> = {}) {
    return this.get<PortfolioTrends>('/portfolio/trends', filters);
  }

  getFilterOptions() {
    return this.get<PortfolioFilterOptions>('/portfolio/filters/options');
  }

  /** Dev smoke probe — Actuator is not under `/api`; proxied at `/actuator`. */
  getHealthProbe() {
    return this.getAtRoot<{ status: string }>('/actuator/health');
  }
}
