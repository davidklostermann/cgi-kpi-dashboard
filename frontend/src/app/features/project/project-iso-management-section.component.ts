import { Component, effect, inject, input, signal } from '@angular/core';
import { take } from 'rxjs';

import { ProjectApiService } from '../../core/api/project-api.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';
import { ProjectIsoManagement } from '../../shared/models/project-iso-management.model';

type LoadStatus = 'loading' | 'success' | 'error';

@Component({
  selector: 'app-project-iso-management-section',
  imports: [StatusBadgeComponent],
  templateUrl: './project-iso-management-section.component.html',
  styleUrl: './project-iso-management-section.component.scss',
})
export class ProjectIsoManagementSectionComponent {
  private readonly projectApi = inject(ProjectApiService);
  private loadGeneration = 0;

  readonly projectId = input.required<string>();
  readonly status = signal<LoadStatus>('loading');
  readonly data = signal<ProjectIsoManagement | null>(null);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    effect(() => this.load(this.projectId()));
  }

  load(projectId = this.projectId()): void {
    const generation = ++this.loadGeneration;
    this.status.set('loading');
    this.errorMessage.set(null);
    this.projectApi
      .getProjectIsoManagement(projectId)
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
    const payload = this.data();
    return !!payload?.dataAvailable;
  }

  emptyMessage(): string {
    const reason = this.data()?.emptyReason?.trim();
    if (reason && !/iso/i.test(reason)) {
      return reason;
    }
    return 'Keine erweiterten Steuerungsdaten für dieses Projekt hinterlegt.';
  }

  categoryLabel(key: string): string {
    const labels: Record<string, string> = {
      benefits: 'Nutzen',
      scope: 'Scope',
      changeRequests: 'Changes',
      quality: 'Qualität',
      stakeholders: 'Stakeholder',
    };
    return labels[key] || key.toUpperCase();
  }

  trendIcon(trend: string): string {
    switch (trend) {
      case 'IMPROVING':
        return 'trending_up';
      case 'STABLE':
        return 'trending_flat';
      case 'DETERIORATING':
        return 'trending_down';
      default:
        return 'help_outline';
    }
  }

  impactIcon(level: string): string {
    return level === 'NONE' ? 'check_circle' : 'priority_high';
  }

  isCritical(value: number | string): boolean {
    if (typeof value === 'number') return value > 0;
    return /kritisch|eskala|hoch/i.test(value);
  }

  factsBadge(): string | null {
    const payload = this.data();
    if (!payload?.factsAsOf) {
      return null;
    }
    const formatted = this.formatDate(payload.factsAsOf);
    return formatted === '—' ? null : `Datenstand ${formatted}`;
  }

  benefitsBadgeStatus(status: string): string {
    switch (status) {
      case 'GREEN':
        return 'ON_TRACK';
      case 'AMBER':
        return 'AT_RISK';
      case 'RED':
        return 'CRITICAL';
      default:
        return 'ON_TRACK';
    }
  }

  clampPercent(value: number): number {
    if (!Number.isFinite(value)) {
      return 0;
    }
    return Math.min(100, Math.max(0, value));
  }

  truncate(value: string | null | undefined, max: number): string {
    if (!value?.trim()) {
      return '—';
    }
    const text = value.trim();
    if (text.length <= max) {
      return text;
    }
    return `${text.slice(0, Math.max(0, max - 1)).trimEnd()}…`;
  }

  isEscalated(status: string | null | undefined): boolean {
    if (!status) {
      return false;
    }
    return /eskala|kritisch|aktiv/i.test(status);
  }

  formatDate(value: string | null | undefined): string {
    if (!value?.trim()) {
      return '—';
    }
    const dateOnly = /^(\d{4})-(\d{2})-(\d{2})/.exec(value.trim());
    if (dateOnly) {
      const [, year, month, day] = dateOnly;
      return new Intl.DateTimeFormat('de-DE').format(
        new Date(Number(year), Number(month) - 1, Number(day)),
      );
    }
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
      return '—';
    }
    return new Intl.DateTimeFormat('de-DE').format(parsed);
  }

  private resolveError(_error: unknown): string {
    return 'Erweiterte Steuerungsdaten konnten nicht geladen werden.';
  }
}
