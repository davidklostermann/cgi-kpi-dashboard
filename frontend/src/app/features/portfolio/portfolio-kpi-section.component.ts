import { Component, effect, inject, signal } from '@angular/core';
import { take } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { PortfolioApiService } from '../../core/api/portfolio-api.service';
import { KpiCardComponent } from '../../shared/components/kpi-card.component';
import { PortfolioKpiSummary } from '../../shared/models/portfolio-kpi.model';
import { PortfolioFilterService } from './portfolio-filter.service';

type LoadStatus = 'loading' | 'success' | 'error';

/** Portfolio KPI facts panel — isolated load/error state (AD-7). */
@Component({
  selector: 'app-portfolio-kpi-section',
  imports: [KpiCardComponent],
  templateUrl: './portfolio-kpi-section.component.html',
  styleUrl: './portfolio-kpi-section.component.scss',
})
export class PortfolioKpiSectionComponent {
  private readonly portfolioApi = inject(PortfolioApiService);
  private readonly filterService = inject(PortfolioFilterService);

  readonly status = signal<LoadStatus>('loading');
  readonly kpis = signal<PortfolioKpiSummary | null>(null);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    effect(() => {
      this.filterService.filters();
      this.load();
    });
  }

  load(): void {
    this.status.set('loading');
    this.errorMessage.set(null);

    this.portfolioApi
      .getPortfolioKpis(this.filterService.toQueryParams())
      .pipe(take(1))
      .subscribe({
        next: (summary) => {
          this.kpis.set(summary);
          this.status.set('success');
        },
        error: (error: unknown) => {
          this.kpis.set(null);
          this.errorMessage.set(this.resolveErrorMessage(error));
          this.status.set('error');
        },
      });
  }

  resetFilters(): void {
    this.filterService.reset();
  }

  hasActiveFilters(): boolean {
    return this.filterService.hasActiveFilters();
  }

  formatPercent(value: number): string {
    return value.toLocaleString('de-DE', { minimumFractionDigits: 1, maximumFractionDigits: 1 });
  }

  formatSignedPercent(value: number): string {
    const formatted = this.formatPercent(Math.abs(value));
    if (value > 0) {
      return `+${formatted}`;
    }
    if (value < 0) {
      return `−${formatted}`;
    }
    return formatted;
  }

  formatStatusDistribution(summary: PortfolioKpiSummary): string {
    const { onTrack, atRisk, critical, completed } = summary.statusDistribution;
    const parts = [
      `Grün: ${onTrack}`,
      `Gelb: ${atRisk}`,
      `Rot: ${critical}`,
    ];
    if (completed > 0) {
      parts.push(`Abgeschlossen: ${completed}`);
    }
    return parts.join(' · ');
  }

  projectCountLabel(): string {
    const lifecycle = this.filterService.filters().lifecycle;
    if (lifecycle === 'completed') {
      return 'Abgeschlossene Projekte';
    }
    if (lifecycle === 'all') {
      return 'Projekte';
    }
    return 'Aktive Projekte';
  }

  projectCountValue(summary: PortfolioKpiSummary): number {
    const lifecycle = this.filterService.filters().lifecycle;
    if (lifecycle === 'completed') {
      return summary.statusDistribution.completed;
    }
    if (lifecycle === 'all') {
      return summary.activeProjectCount + summary.statusDistribution.completed;
    }
    return summary.activeProjectCount;
  }

  private resolveErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const body = error.error as { message?: string } | null;
      if (body?.message) {
        return body.message;
      }
      if (error.status === 0) {
        return 'Die Portfolio-KPIs konnten nicht geladen werden. Bitte prüfen Sie die Verbindung zum Backend.';
      }
    }
    return 'Die Portfolio-KPIs konnten nicht geladen werden. Bitte versuchen Sie es erneut.';
  }
}
