import { Injectable } from '@angular/core';

import { ApiClient } from './api-client.service';
import { PortfolioFilterOptions } from '../../shared/models/portfolio-filter.model';
import { PortfolioKpiSummary } from '../../shared/models/portfolio-kpi.model';

/** Portfolio facts API (FR-1, FR-8). */
@Injectable({ providedIn: 'root' })
export class PortfolioApiService extends ApiClient {
  getPortfolioKpis(filters: Record<string, string | string[]> = {}) {
    return this.get<PortfolioKpiSummary>('/portfolio/kpis', filters);
  }

  getFilterOptions() {
    return this.get<PortfolioFilterOptions>('/portfolio/filters/options');
  }

  /** Dev smoke probe — Actuator is not under `/api`; proxied at `/actuator`. */
  getHealthProbe() {
    return this.getAtRoot<{ status: string }>('/actuator/health');
  }
}
