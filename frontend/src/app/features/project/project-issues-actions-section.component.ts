import { Component, effect, inject, input, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { take } from 'rxjs';

import { ProjectApiService } from '../../core/api/project-api.service';
import {
  IssueActionItem,
  ProjectIssuesActions,
} from '../../shared/models/project-issues-capacity.model';

type LoadStatus = 'loading' | 'success' | 'error';

@Component({
  selector: 'app-project-issues-actions-section',
  templateUrl: './project-issues-actions-section.component.html',
  styleUrl: './project-issues-actions-section.component.scss',
})
export class ProjectIssuesActionsSectionComponent {
  private readonly projectApi = inject(ProjectApiService);
  private loadGeneration = 0;

  readonly projectId = input.required<string>();
  readonly status = signal<LoadStatus>('loading');
  readonly data = signal<ProjectIssuesActions | null>(null);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    effect(() => this.load(this.projectId()));
  }

  load(projectId = this.projectId()): void {
    const generation = ++this.loadGeneration;
    this.status.set('loading');
    this.errorMessage.set(null);
    this.projectApi
      .getProjectIssuesActions(projectId)
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

  /** Entscheidungsblock nur bei Eskalationsbedarf (`escalationNeeded`). */
  escalationItems(items: IssueActionItem[]): IssueActionItem[] {
    return items.filter((item) => item.escalationNeeded);
  }

  decisionsHeading(count: number): string {
    return count === 1 ? 'Benötigte Entscheidung' : 'Benötigte Entscheidungen';
  }

  decisionText(item: IssueActionItem): string {
    const fromAction = item.nextAction?.trim() || item.actionText?.trim();
    if (fromAction) {
      return fromAction;
    }
    return 'nicht hinterlegt';
  }

  decisionOwner(item: IssueActionItem): string {
    const who = item.requiredDecision?.decideWho?.trim() || item.owner?.trim();
    return who || 'nicht hinterlegt';
  }

  decisionDue(item: IssueActionItem): string {
    const due = item.requiredDecision?.decideBy ?? item.dueDate;
    return this.formatDate(due);
  }

  decisionImpact(item: IssueActionItem): string {
    const impact = item.requiredDecision?.impactIfDeferred?.trim();
    return impact || 'nicht hinterlegt';
  }

  formatDate(value: string | null | undefined): string {
    if (!value?.trim()) {
      return 'nicht hinterlegt';
    }
    const trimmed = value.trim();
    const dateOnly = /^(\d{4})-(\d{2})-(\d{2})$/.exec(trimmed);
    if (dateOnly) {
      const [, year, month, day] = dateOnly;
      return new Intl.DateTimeFormat('de-DE').format(
        new Date(Number(year), Number(month) - 1, Number(day)),
      );
    }
    const parsed = new Date(trimmed);
    if (Number.isNaN(parsed.getTime())) {
      return 'nicht hinterlegt';
    }
    return new Intl.DateTimeFormat('de-DE').format(parsed);
  }

  severityTone(severity: string | null | undefined): 'critical' | 'high' | 'neutral' {
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
