import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { PortfolioApiService } from '../../core/api/portfolio-api.service';
import { PortfolioFilterService } from './portfolio-filter.service';
import { PortfolioFilterOptions } from '../../shared/models/portfolio-filter.model';

/** Portfolio filter controls (UX filter-bar, FR-8). */
@Component({
  selector: 'app-portfolio-filter-bar',
  templateUrl: './portfolio-filter-bar.component.html',
  styleUrl: './portfolio-filter-bar.component.scss',
})
export class PortfolioFilterBarComponent {
  private readonly portfolioApi = inject(PortfolioApiService);
  private readonly filterService = inject(PortfolioFilterService);
  private readonly destroyRef = inject(DestroyRef);

  readonly options = signal<PortfolioFilterOptions | null>(null);
  readonly filters = this.filterService.filters;

  constructor() {
    this.portfolioApi
      .getFilterOptions()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((options) => this.options.set(options));
  }

  onCustomerChange(event: Event): void {
    this.filterService.update({ customer: this.readValue(event) });
  }

  onProjectLeadChange(event: Event): void {
    this.filterService.update({ projectLead: this.readValue(event) });
  }

  onStatusChange(event: Event): void {
    const value = this.readValue(event);
    this.filterService.update({ statuses: value ? [value] : [] });
  }

  onPhaseChange(event: Event): void {
    this.filterService.update({ phase: this.readValue(event) });
  }

  onLifecycleChange(event: Event): void {
    this.filterService.update({
      lifecycle: this.readValue(event) as '' | 'active' | 'completed' | 'all',
    });
  }

  onReportMonthChange(event: Event): void {
    this.filterService.update({ reportMonth: this.readValue(event) });
  }

  onRiskSeverityChange(event: Event): void {
    this.filterService.update({ riskSeverity: this.readValue(event) });
  }

  resetFilters(): void {
    this.filterService.reset();
  }

  statusLabel(status: string): string {
    return (
      {
        ON_TRACK: 'Grün',
        AT_RISK: 'Gelb',
        CRITICAL: 'Rot',
        COMPLETED: 'Abgeschlossen',
      }[status] ?? status
    );
  }

  private readValue(event: Event): string {
    return (event.target as HTMLInputElement | HTMLSelectElement).value;
  }
}
