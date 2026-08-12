import { Injectable, signal } from '@angular/core';

import {
  EMPTY_PORTFOLIO_FILTERS,
  PortfolioFilters,
} from '../../shared/models/portfolio-filter.model';

/** Shared portfolio filter state — root singleton preserves filters across navigation (FR-7 / Story 5.5). */
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
    const statuses = current.statuses ?? [];
    const deliveryMethods = current.deliveryMethods ?? [];
    return (
      !!current.customer ||
      !!current.projectLead ||
      statuses.length > 0 ||
      !!current.phase ||
      !!current.lifecycle ||
      !!current.reportMonth ||
      !!current.riskSeverity ||
      deliveryMethods.length > 0
    );
  }

  toQueryParams(): Record<string, string | string[]> {
    const current = this.filters();
    const params: Record<string, string | string[]> = {};
    const statuses = current.statuses ?? [];
    const deliveryMethods = current.deliveryMethods ?? [];

    if (current.customer) {
      params['customer'] = current.customer;
    }
    if (current.projectLead) {
      params['projectLead'] = current.projectLead;
    }
    if (statuses.length > 0) {
      params['status'] = statuses;
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
    if (deliveryMethods.length > 0) {
      params['deliveryMethod'] = deliveryMethods;
    }

    return params;
  }
}
