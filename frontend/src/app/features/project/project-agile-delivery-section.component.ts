import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { take } from 'rxjs';

import { ProjectApiService } from '../../core/api/project-api.service';
import {
  AgileDelivery,
  AgileSprint,
  AgileSprintChart,
} from '../../shared/models/agile-delivery.model';

type LoadStatus = 'loading' | 'success' | 'error';

@Component({
  selector: 'app-project-agile-delivery-section',
  templateUrl: './project-agile-delivery-section.component.html',
  styleUrl: './project-agile-delivery-section.component.scss',
})
export class ProjectAgileDeliverySectionComponent {
  private readonly projectApi = inject(ProjectApiService);
  private loadGeneration = 0;

  readonly projectId = input.required<string>();
  readonly status = signal<LoadStatus>('loading');
  readonly data = signal<AgileDelivery | null>(null);
  readonly errorMessage = signal<string | null>(null);

  readonly chartMax = computed(() => {
    const chart = this.data()?.chart;
    if (!chart) {
      return 1;
    }
    return Math.max(
      1,
      ...chart.plannedStoryPoints,
      ...chart.completedStoryPoints,
      ...chart.velocityTrend.filter((value): value is number => value !== null),
    );
  });

  constructor() {
    effect(() => this.load(this.projectId()));
  }

  load(projectId = this.projectId()): void {
    const generation = ++this.loadGeneration;
    this.status.set('loading');
    this.errorMessage.set(null);

    this.projectApi
      .getAgileDelivery(projectId)
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

  hasData(): boolean {
    return this.data()?.dataAvailable === true;
  }

  emptyMessage(): string {
    return (
      this.data()?.emptyReason?.trim() ??
      'Keine Sprint-Daten für dieses Projekt hinterlegt.'
    );
  }

  progressWidth(sprint: AgileSprint): number {
    return Math.min(100, Math.max(0, sprint.progressPercent));
  }

  healthClass(health: AgileSprint['health']): string {
    return `agile-delivery__health agile-delivery__health--${health.toLowerCase()}`;
  }

  chartBarHeight(value: number): number {
    return (Math.max(0, value) / this.chartMax()) * 100;
  }

  chartLinePoints(chart: AgileSprintChart): string {
    const values = chart.velocityTrend;
    const width = 100;
    const height = 100;
    const steps = Math.max(1, values.length - 1);
    return values
      .map((value, index) => {
        if (value === null) {
          return null;
        }
        const x = (index / steps) * width;
        const y = height - (value / this.chartMax()) * height;
        return `${x},${y}`;
      })
      .filter((point): point is string => point !== null)
      .join(' ');
  }

  formatVelocity(value: number | null): string {
    return value === null ? '—' : value.toLocaleString('de-DE', { maximumFractionDigits: 1 });
  }

  dataStandLabel(value: string | null): string {
    if (!value) {
      return 'Datenstand nicht verfügbar';
    }
    return `Datenstand ${new Intl.DateTimeFormat('de-DE', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    }).format(new Date(value))}`;
  }

  private resolveError(_error: unknown): string {
    return 'Agile Delivery konnte nicht geladen werden.';
  }
}
