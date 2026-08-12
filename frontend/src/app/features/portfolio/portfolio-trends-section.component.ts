import { Component, effect, inject, signal } from '@angular/core';
import { take } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { PortfolioApiService } from '../../core/api/portfolio-api.service';
import { TrendChartComponent } from '../../shared/components/trend-chart.component';
import { PortfolioTrends } from '../../shared/models/portfolio-trends.model';
import { PortfolioFilterService } from './portfolio-filter.service';

type LoadStatus = 'loading' | 'success' | 'error';

@Component({
  selector: 'app-portfolio-trends-section',
  imports: [TrendChartComponent],
  templateUrl: './portfolio-trends-section.component.html',
  styleUrl: './portfolio-trends-section.component.scss',
})
export class PortfolioTrendsSectionComponent {
  private readonly portfolioApi = inject(PortfolioApiService);
  private readonly filterService = inject(PortfolioFilterService);

  readonly status = signal<LoadStatus>('loading');
  readonly trends = signal<PortfolioTrends | null>(null);
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
      .getPortfolioTrends(this.filterService.toQueryParams())
      .pipe(take(1))
      .subscribe({
        next: (data) => {
          this.trends.set(data);
          this.status.set('success');
        },
        error: (error: unknown) => {
          this.trends.set(null);
          this.errorMessage.set(this.resolveErrorMessage(error));
          this.status.set('error');
        },
      });
  }

  private resolveErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const body = error.error as { message?: string } | null;
      if (body?.message) {
        return body.message;
      }
      if (error.status === 0) {
        return 'Die Portfolio-Trends konnten nicht geladen werden. Bitte prüfen Sie die Verbindung zum Backend.';
      }
    }
    return 'Die Portfolio-Trends konnten nicht geladen werden. Bitte versuchen Sie es erneut.';
  }
}
