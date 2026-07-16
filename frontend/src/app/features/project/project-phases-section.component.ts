import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { take } from 'rxjs';

import { ProjectApiService } from '../../core/api/project-api.service';
import { GanttTimelineComponent } from '../../shared/components/gantt-timeline.component';
import { ProjectPhases, toGanttProject } from '../../shared/models/project-phases.model';

type LoadStatus = 'loading' | 'success' | 'error';

@Component({
  selector: 'app-project-phases-section',
  imports: [GanttTimelineComponent],
  templateUrl: './project-phases-section.component.html',
  styleUrl: './project-phases-section.component.scss',
})
export class ProjectPhasesSectionComponent {
  private readonly projectApi = inject(ProjectApiService);

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
    this.status.set('loading');
    this.errorMessage.set(null);
    this.projectApi
      .getProjectPhases(projectId)
      .pipe(take(1))
      .subscribe({
        next: (phases) => {
          this.phases.set(phases);
          this.status.set('success');
        },
        error: (error: unknown) => {
          this.phases.set(null);
          this.errorMessage.set(this.resolveErrorMessage(error));
          this.status.set('error');
        },
      });
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
