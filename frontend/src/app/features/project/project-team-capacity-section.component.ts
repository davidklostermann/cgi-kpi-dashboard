import { Component, effect, inject, input, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { take } from 'rxjs';

import { ProjectApiService } from '../../core/api/project-api.service';
import { ProjectCapacity } from '../../shared/models/project-issues-capacity.model';

type LoadStatus = 'loading' | 'success' | 'error';

@Component({
  selector: 'app-project-team-capacity-section',
  templateUrl: './project-team-capacity-section.component.html',
  styleUrl: './project-team-capacity-section.component.scss',
})
export class ProjectTeamCapacitySectionComponent {
  private readonly projectApi = inject(ProjectApiService);
  private loadGeneration = 0;

  readonly projectId = input.required<string>();
  readonly status = signal<LoadStatus>('loading');
  readonly data = signal<ProjectCapacity | null>(null);
  readonly errorMessage = signal<string | null>(null);

  /** True when at least one role capacity row exists. */
  hasRoles(): boolean {
    const payload = this.data();
    return !!payload && payload.roles.length > 0;
  }

  constructor() {
    effect(() => this.load(this.projectId()));
  }

  load(projectId = this.projectId()): void {
    const generation = ++this.loadGeneration;
    this.status.set('loading');
    this.errorMessage.set(null);
    this.projectApi
      .getProjectCapacity(projectId)
      .pipe(take(1))
      .subscribe({
        next: (payload) => {
          if (generation !== this.loadGeneration || projectId !== this.projectId()) {
            return;
          }
          this.data.set(payload);
          this.status.set('success');
        },
        error: (error: unknown) => {
          if (generation !== this.loadGeneration || projectId !== this.projectId()) {
            return;
          }
          this.data.set(null);
          this.errorMessage.set(this.resolveError(error));
          this.status.set('error');
        },
      });
  }

  formatDate(value: string | null): string {
    if (!value) {
      return 'Nicht verfügbar';
    }
    const match = /^(\d{4})-(\d{2})-(\d{2})/.exec(value);
    if (match) {
      return `${match[3]}.${match[2]}.`;
    }
    return new Intl.DateTimeFormat('de-DE', {
      day: '2-digit',
      month: '2-digit',
    }).format(new Date(value));
  }

  formatFte(value: number | null | undefined): string {
    if (value == null || Number.isNaN(value)) {
      return '—';
    }
    return new Intl.NumberFormat('de-DE', {
      minimumFractionDigits: 0,
      maximumFractionDigits: 2,
    }).format(value);
  }

  private resolveError(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const body = error.error as { message?: string } | null;
      if (body?.message) {
        return body.message;
      }
    }
    return 'Team- und Kapazitätsdaten konnten nicht geladen werden.';
  }
}
