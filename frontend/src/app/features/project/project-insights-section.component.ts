import { Component, effect, inject, input, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { take } from 'rxjs';

import { ProjectApiService } from '../../core/api/project-api.service';
import { ProjectInsights } from '../../shared/models/project-insights.model';

type LoadStatus = 'loading' | 'success' | 'error';

@Component({
  selector: 'app-project-insights-section',
  templateUrl: './project-insights-section.component.html',
  styleUrl: './project-insights-section.component.scss',
})
export class ProjectInsightsSectionComponent {
  private readonly projectApi = inject(ProjectApiService);

  readonly projectId = input.required<string>();
  readonly status = signal<LoadStatus>('loading');
  readonly insights = signal<ProjectInsights | null>(null);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    effect(() => this.load(this.projectId()));
  }

  load(projectId = this.projectId()): void {
    this.status.set('loading');
    this.projectApi
      .getProjectInsights(projectId)
      .pipe(take(1))
      .subscribe({
        next: (data) => {
          this.insights.set(data);
          this.status.set('success');
        },
        error: (error: unknown) => {
          this.insights.set(null);
          this.errorMessage.set(
            error instanceof HttpErrorResponse && (error.error as { message?: string })?.message
              ? (error.error as { message: string }).message
              : 'Management Insights konnten nicht geladen werden.',
          );
          this.status.set('error');
        },
      });
  }
}
