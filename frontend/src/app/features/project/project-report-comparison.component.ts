import { Component, effect, inject, input, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { take } from 'rxjs';

import { ProjectApiService } from '../../core/api/project-api.service';
import { ProjectTrends } from '../../shared/models/project-insights.model';

type LoadStatus = 'loading' | 'success' | 'error';

@Component({
  selector: 'app-project-report-comparison',
  templateUrl: './project-report-comparison.component.html',
  styleUrl: './project-report-comparison.component.scss',
})
export class ProjectReportComparisonComponent {
  private readonly projectApi = inject(ProjectApiService);

  readonly projectId = input.required<string>();
  readonly status = signal<LoadStatus>('loading');
  readonly trends = signal<ProjectTrends | null>(null);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    effect(() => this.load(this.projectId()));
  }

  load(projectId = this.projectId()): void {
    this.status.set('loading');
    this.projectApi
      .getProjectTrends(projectId)
      .pipe(take(1))
      .subscribe({
        next: (data) => {
          this.trends.set(data);
          this.status.set('success');
        },
        error: (error: unknown) => {
          this.trends.set(null);
          this.errorMessage.set(
            error instanceof HttpErrorResponse && (error.error as { message?: string })?.message
              ? (error.error as { message: string }).message
              : 'Berichtsstandsvergleich konnte nicht geladen werden.',
          );
          this.status.set('error');
        },
      });
  }

  formatSigned(value: number | null | undefined, suffix = ''): string {
    if (value == null) {
      return '—';
    }
    const abs = Math.abs(value).toLocaleString('de-DE');
    const sign = value > 0 ? '+' : value < 0 ? '−' : '';
    return `${sign}${abs}${suffix}`;
  }
}
