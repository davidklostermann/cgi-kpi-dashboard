import { Component, effect, inject, input, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { take } from 'rxjs';

import { ProjectApiService } from '../../core/api/project-api.service';
import { ProjectIssuesActions } from '../../shared/models/project-issues-capacity.model';

type LoadStatus = 'loading' | 'success' | 'error';

@Component({
  selector: 'app-project-issues-actions-section',
  templateUrl: './project-issues-actions-section.component.html',
  styleUrl: './project-issues-actions-section.component.scss',
})
export class ProjectIssuesActionsSectionComponent {
  private readonly projectApi = inject(ProjectApiService);

  readonly projectId = input.required<string>();
  readonly status = signal<LoadStatus>('loading');
  readonly data = signal<ProjectIssuesActions | null>(null);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    effect(() => this.load(this.projectId()));
  }

  load(projectId = this.projectId()): void {
    this.status.set('loading');
    this.errorMessage.set(null);
    this.projectApi
      .getProjectIssuesActions(projectId)
      .pipe(take(1))
      .subscribe({
        next: (payload) => {
          this.data.set(payload);
          this.status.set('success');
        },
        error: (error: unknown) => {
          this.data.set(null);
          this.errorMessage.set(this.resolveError(error));
          this.status.set('error');
        },
      });
  }

  formatDate(value: string | null): string {
    if (!value) {
      return 'Nicht gesetzt';
    }
    return new Intl.DateTimeFormat('de-DE').format(new Date(value));
  }

  severityTone(severity: string): 'critical' | 'high' | 'neutral' {
    const normalized = severity?.toUpperCase() ?? '';
    if (normalized === 'CRITICAL') {
      return 'critical';
    }
    if (normalized === 'HIGH') {
      return 'high';
    }
    return 'neutral';
  }

  private resolveError(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const body = error.error as { message?: string } | null;
      if (body?.message) {
        return body.message;
      }
    }
    return 'Probleme, Risiken und Maßnahmen konnten nicht geladen werden.';
  }
}
