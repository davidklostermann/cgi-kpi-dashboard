import { Component, effect, inject, input, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { take } from 'rxjs';

import { ProjectApiService } from '../../core/api/project-api.service';
import { ProjectTrends } from '../../shared/models/project-insights.model';

type LoadStatus = 'loading' | 'success' | 'error';
export type ComparisonTone = 'improved' | 'worsened' | 'unchanged' | 'neutral';

export interface ComparisonCard {
  id: string;
  metric: string;
  change: string;
  assessment: string;
  tone: ComparisonTone;
  icon: string;
}

@Component({
  selector: 'app-project-report-comparison',
  templateUrl: './project-report-comparison.component.html',
  styleUrl: './project-report-comparison.component.scss',
})
export class ProjectReportComparisonComponent {
  private readonly projectApi = inject(ProjectApiService);
  private loadGeneration = 0;

  readonly projectId = input.required<string>();
  readonly status = signal<LoadStatus>('loading');
  readonly trends = signal<ProjectTrends | null>(null);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    effect(() => this.load(this.projectId()));
  }

  load(projectId = this.projectId()): void {
    const generation = ++this.loadGeneration;
    this.status.set('loading');
    this.projectApi
      .getProjectTrends(projectId)
      .pipe(take(1))
      .subscribe({
        next: (data) => {
          if (generation !== this.loadGeneration || projectId !== this.projectId()) {
            return;
          }
          this.trends.set(data);
          this.status.set('success');
        },
        error: (error: unknown) => {
          if (generation !== this.loadGeneration || projectId !== this.projectId()) {
            return;
          }
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

  formatSnapshotDate(value: string | null | undefined): string {
    if (!value?.trim()) {
      return '—';
    }
    const trimmed = value.trim();
    const dateOnly = /^(\d{4})-(\d{2})-(\d{2})/.exec(trimmed);
    if (dateOnly) {
      const [, year, month, day] = dateOnly;
      return new Intl.DateTimeFormat('de-DE').format(
        new Date(Number(year), Number(month) - 1, Number(day)),
      );
    }
    const parsed = new Date(trimmed);
    if (Number.isNaN(parsed.getTime())) {
      return trimmed;
    }
    return new Intl.DateTimeFormat('de-DE').format(parsed);
  }

  comparisonCards(data: ProjectTrends): ComparisonCard[] {
    const cards: ComparisonCard[] = [];

    if (this.isFiniteNumber(data.progressDeltaPercent)) {
      const delta = data.progressDeltaPercent;
      const abs = Math.abs(delta).toLocaleString('de-DE');
      cards.push({
        id: 'progress',
        metric: 'Fortschritt',
        change:
          delta === 0
            ? 'Unverändert'
            : delta > 0
              ? `+${abs} Prozentpunkte`
              : `−${abs} Prozentpunkte`,
        assessment:
          delta === 0 ? 'Stabil' : delta > 0 ? 'Verbesserung' : 'Verschlechterung',
        tone: delta === 0 ? 'unchanged' : delta > 0 ? 'improved' : 'worsened',
        icon: delta === 0 ? '→' : delta > 0 ? '↑' : '↓',
      });
    }

    if (this.isFiniteNumber(data.budgetActualDelta)) {
      const delta = data.budgetActualDelta;
      const abs = Math.abs(delta).toLocaleString('de-DE');
      cards.push({
        id: 'costs',
        metric: 'Ist-Kosten',
        change: delta === 0 ? 'Unverändert' : delta > 0 ? `+${abs} €` : `−${abs} €`,
        assessment:
          delta === 0
            ? 'Stabil'
            : delta > 0
              ? 'Kosten gestiegen'
              : 'Kosten gesunken',
        tone: delta === 0 ? 'unchanged' : delta > 0 ? 'worsened' : 'improved',
        icon: delta === 0 ? '→' : delta > 0 ? '↑' : '↓',
      });
    }

    if (this.isFiniteNumber(data.scheduleDeviationDeltaDays)) {
      const delta = data.scheduleDeviationDeltaDays;
      const abs = Math.abs(delta).toLocaleString('de-DE');
      cards.push({
        id: 'schedule',
        metric: 'Termin',
        change:
          delta === 0
            ? 'Unverändert'
            : delta > 0
              ? `+${abs} Tage Abweichung`
              : `−${abs} Tage Abweichung`,
        assessment:
          delta === 0
            ? 'Stabil'
            : delta > 0
              ? 'Verschlechterung'
              : 'Verbesserung',
        tone: delta === 0 ? 'unchanged' : delta > 0 ? 'worsened' : 'improved',
        icon: delta === 0 ? '→' : delta > 0 ? '↓' : '↑',
      });
    }

    if (data.previousStatusLabel && data.currentStatusLabel) {
      const tone = this.statusTone(
        data.previousStatus,
        data.currentStatus,
        data.previousStatusLabel,
        data.currentStatusLabel,
      );
      cards.push({
        id: 'status',
        metric: 'Status',
        change:
          data.previousStatusLabel === data.currentStatusLabel
            ? data.currentStatusLabel
            : `${data.previousStatusLabel} → ${data.currentStatusLabel}`,
        assessment:
          tone === 'unchanged'
            ? 'Unverändert'
            : tone === 'improved'
              ? 'Verbesserung'
              : tone === 'worsened'
                ? 'Verschlechterung'
                : 'Statuswechsel',
        tone,
        icon: tone === 'improved' ? '↑' : tone === 'worsened' ? '↓' : '→',
      });
    }

    if (this.isFiniteNumber(data.openRiskCountDelta)) {
      const delta = data.openRiskCountDelta;
      const abs = Math.abs(delta).toLocaleString('de-DE');
      cards.push({
        id: 'risks',
        metric: 'Risiken',
        change:
          delta === 0
            ? 'Unverändert'
            : delta > 0
              ? `+${abs} offene Risiken`
              : `−${abs} offene Risiken`,
        assessment:
          delta === 0
            ? 'Stabil'
            : delta > 0
              ? 'Verschlechterung'
              : 'Verbesserung',
        tone: delta === 0 ? 'unchanged' : delta > 0 ? 'worsened' : 'improved',
        icon: delta === 0 ? '→' : delta > 0 ? '↑' : '↓',
      });
    }

    return cards;
  }

  private isFiniteNumber(value: number | null | undefined): value is number {
    return value != null && Number.isFinite(value);
  }

  private statusTone(
    previous: string | null | undefined,
    current: string | null | undefined,
    previousLabel: string,
    currentLabel: string,
  ): ComparisonTone {
    if (!previous || !current) {
      return previousLabel === currentLabel ? 'unchanged' : 'neutral';
    }
    if (previous === current) {
      return 'unchanged';
    }
    const rank = (code: string): number => {
      switch (code.toUpperCase()) {
        case 'ON_TRACK':
          return 0;
        case 'AT_RISK':
          return 1;
        case 'CRITICAL':
        case 'OFF_TRACK':
          return 2;
        default:
          return 1;
      }
    };
    const delta = rank(current) - rank(previous);
    if (delta === 0) {
      return 'neutral';
    }
    return delta < 0 ? 'improved' : 'worsened';
  }
}
