import { Component, effect, inject, signal } from '@angular/core';
import { take } from 'rxjs';

import { PortfolioApiService } from '../../core/api/portfolio-api.service';
import { GanttTimelineComponent } from '../../shared/components/gantt-timeline.component';
import { PortfolioTimeline } from '../../shared/models/portfolio-timeline.model';
import { resolveLoadError } from '../../shared/utils/ai-error.util';
import { PortfolioFilterService } from './portfolio-filter.service';

type LoadStatus = 'loading' | 'success' | 'error';

/** Portfolio Gantt section — isolated load/error state (AD-7). */
@Component({
  selector: 'app-portfolio-gantt-section',
  imports: [GanttTimelineComponent],
  templateUrl: './portfolio-gantt-section.component.html',
  styleUrl: './portfolio-gantt-section.component.scss',
})
export class PortfolioGanttSectionComponent {
  private readonly portfolioApi = inject(PortfolioApiService);
  private readonly filterService = inject(PortfolioFilterService);

  readonly status = signal<LoadStatus>('loading');
  readonly timeline = signal<PortfolioTimeline | null>(null);
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
      .getPortfolioTimeline(this.filterService.toQueryParams())
      .pipe(take(1))
      .subscribe({
        next: (data) => {
          this.timeline.set(data);
          this.status.set('success');
        },
        error: (error: unknown) => {
          this.timeline.set(null);
          this.errorMessage.set(this.resolveErrorMessage(error));
          this.status.set('error');
        },
      });
  }

  private resolveErrorMessage(error: unknown): string {
    return resolveLoadError(
      error,
      'Die Portfolio-Zeitleiste konnte nicht geladen werden. Bitte versuchen Sie es erneut.',
    );
  }
}
