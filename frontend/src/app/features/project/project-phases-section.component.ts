import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { take } from 'rxjs';

import { ProjectApiService } from '../../core/api/project-api.service';
import { GanttTimelineComponent } from '../../shared/components/gantt-timeline.component';
import {
  ProjectMilestoneItem,
  ProjectPhases,
  toGanttProject,
} from '../../shared/models/project-phases.model';

type LoadStatus = 'loading' | 'success' | 'error';

@Component({
  selector: 'app-project-phases-section',
  imports: [GanttTimelineComponent],
  templateUrl: './project-phases-section.component.html',
  styleUrl: './project-phases-section.component.scss',
})
export class ProjectPhasesSectionComponent {
  private readonly projectApi = inject(ProjectApiService);
  private loadGeneration = 0;

  readonly projectId = input.required<string>();

  readonly status = signal<LoadStatus>('loading');
  readonly phases = signal<ProjectPhases | null>(null);
  readonly errorMessage = signal<string | null>(null);

  readonly ganttProjects = computed(() => {
    const data = this.phases();
    return data ? [toGanttProject(data)] : [];
  });

  constructor() {
    effect(() => {
      this.load(this.projectId());
    });
  }

  load(projectId = this.projectId()): void {
    const generation = ++this.loadGeneration;
    this.status.set('loading');
    this.errorMessage.set(null);
    this.projectApi
      .getProjectPhases(projectId)
      .pipe(take(1))
      .subscribe({
        next: (phases) => {
          if (generation !== this.loadGeneration || projectId !== this.projectId()) {
            return;
          }
          this.phases.set(phases);
          this.status.set('success');
        },
        error: (error: unknown) => {
          if (generation !== this.loadGeneration || projectId !== this.projectId()) {
            return;
          }
          this.phases.set(null);
          this.errorMessage.set(this.resolveErrorMessage(error));
          this.status.set('error');
        },
      });
  }

  /** Überfällige zuerst, danach nach Plantermin. */
  sortedMilestones(milestones: ProjectMilestoneItem[]): ProjectMilestoneItem[] {
    return [...milestones].sort((a, b) => {
      if (a.overdue !== b.overdue) {
        return a.overdue ? -1 : 1;
      }
      return (a.plannedDueDate ?? '').localeCompare(b.plannedDueDate ?? '');
    });
  }

  formatDate(iso: string | null | undefined): string {
    if (!iso?.trim()) {
      return 'Nicht hinterlegt';
    }
    const match = /^(\d{4})-(\d{2})-(\d{2})/.exec(iso.trim());
    if (!match) {
      return 'Nicht hinterlegt';
    }
    const [, year, month, day] = match;
    return new Intl.DateTimeFormat('de-DE').format(
      new Date(Number(year), Number(month) - 1, Number(day)),
    );
  }

  formatDeviation(days: number): string {
    const abs = Math.abs(days);
    const unit = abs === 1 ? 'Tag' : 'Tage';
    if (days > 0) {
      return `+${abs} ${unit}`;
    }
    if (days < 0) {
      return `−${abs} ${unit}`;
    }
    return 'Keine';
  }

  formatOverdueDays(days: number): string {
    return days === 1 ? 'Überfällig seit 1 Tag' : `Überfällig seit ${days} Tagen`;
  }

  isDone(status: string): boolean {
    const normalized = status?.toUpperCase() ?? '';
    return normalized === 'COMPLETED' || normalized === 'DONE';
  }

  isOverdue(milestone: ProjectMilestoneItem): boolean {
    return milestone.overdue && !this.isDone(milestone.status);
  }

  private resolveErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const body = error.error as { message?: string } | null;
      if (body?.message) {
        return body.message;
      }
    }
    return 'Phasen und Meilensteine konnten nicht geladen werden. Bitte versuchen Sie es erneut.';
  }
}
