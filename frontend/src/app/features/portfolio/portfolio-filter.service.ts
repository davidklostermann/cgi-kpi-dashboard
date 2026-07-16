import { Injectable, signal } from '@angular/core';

import {
  EMPTY_PORTFOLIO_FILTERS,
  PortfolioFilters,
} from '../../shared/models/portfolio-filter.model';

/** Shared portfolio filter state for KPI and future views (FR-8). */
@Injectable({ providedIn: 'root' })
export class PortfolioFilterService {
  readonly filters = signal<PortfolioFilters>({ ...EMPTY_PORTFOLIO_FILTERS });

  update(partial: Partial<PortfolioFilters>): void {
    this.filters.update((current) => ({ ...current, ...partial }));
  }

  reset(): void {
    this.filters.set({ ...EMPTY_PORTFOLIO_FILTERS });
  }

  hasActiveFilters(): boolean {
    const current = this.filters();
    return (
      !!current.customer ||
      !!current.projectLead ||
      current.statuses.length > 0 ||
      !!current.phase ||
      !!current.lifecycle ||
      !!current.reportMonth ||
      !!current.riskSeverity
    );
  }

  toQueryParams(): Record<string, string | string[]> {
    const current = this.filters();
    const params: Record<string, string | string[]> = {};

    if (current.customer) {
      params['customer'] = current.customer;
    }
    if (current.projectLead) {
      params['projectLead'] = current.projectLead;
    }
    if (current.statuses.length > 0) {
      params['status'] = current.statuses;
    }
    if (current.phase) {
      params['phase'] = current.phase;
    }
    if (current.lifecycle) {
      params['lifecycle'] = current.lifecycle;
    }
    if (current.reportMonth) {
      params['reportMonth'] = current.reportMonth;
    }
    if (current.riskSeverity) {
      params['riskSeverity'] = current.riskSeverity;
    }

    return params;
  }
}
