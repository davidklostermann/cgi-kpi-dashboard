import { Component, effect, inject, input, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { take } from 'rxjs';

import { ProjectApiService } from '../../core/api/project-api.service';
import { KpiCardComponent } from '../../shared/components/kpi-card.component';
import { ProjectKpis } from '../../shared/models/project-kpi.model';

type LoadStatus = 'loading' | 'success' | 'error';

/** Project management KPI cards + budget/effort breakdown (FR-5 / Story 6.3). */
@Component({
  selector: 'app-project-kpi-section',
  imports: [KpiCardComponent],
  templateUrl: './project-kpi-section.component.html',
  styleUrl: './project-kpi-section.component.scss',
})
export class ProjectKpiSectionComponent {
  private readonly projectApi = inject(ProjectApiService);

  readonly projectId = input.required<string>();

  readonly status = signal<LoadStatus>('loading');
  readonly kpis = signal<ProjectKpis | null>(null);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    effect(() => {
      this.load(this.projectId());
    });
  }

  load(projectId = this.projectId()): void {
    this.status.set('loading');
    this.errorMessage.set(null);

    this.projectApi
      .getProjectKpis(projectId)
      .pipe(take(1))
      .subscribe({
        next: (kpis) => {
          this.kpis.set(kpis);
          this.status.set('success');
        },
        error: (error: unknown) => {
          this.kpis.set(null);
          this.errorMessage.set(this.resolveErrorMessage(error));
          this.status.set('error');
        },
      });
  }

  formatPercent(value: number | null | undefined): string {
    if (value == null) {
      return '—';
    }
    return value.toLocaleString('de-DE', { minimumFractionDigits: 1, maximumFractionDigits: 1 });
  }

  formatSignedPercent(value: number | null | undefined): string {
    if (value == null) {
      return '—';
    }
    const formatted = this.formatPercent(Math.abs(value));
    if (value > 0) {
      return `+${formatted}`;
    }
    if (value < 0) {
      return `−${formatted}`;
    }
    return formatted;
  }

  formatMoney(value: number | null | undefined): string {
    if (value == null) {
      return '—';
    }
    return value.toLocaleString('de-DE', { maximumFractionDigits: 0 });
  }

  formatDays(value: number | null | undefined): string {
    if (value == null) {
      return '—';
    }
    return value.toLocaleString('de-DE', { maximumFractionDigits: 1 });
  }

  private resolveErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const body = error.error as { message?: string } | null;
      if (body?.message) {
        return body.message;
      }
      if (error.status === 0) {
        return 'Die Projekt-KPIs konnten nicht geladen werden. Bitte prüfen Sie die Verbindung zum Backend.';
      }
    }
    return 'Die Projekt-KPIs konnten nicht geladen werden. Bitte versuchen Sie es erneut.';
  }
}
