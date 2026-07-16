import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { take } from 'rxjs';

import { ProjectApiService } from '../../core/api/project-api.service';
import { FactsAiLayoutComponent } from '../../core/layout/facts-ai-layout.component';
import { BreadcrumbsComponent } from '../../core/navigation/breadcrumbs.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';
import { ProjectMasterData } from '../../shared/models/project-detail.model';
import { AiPanelPlaceholderComponent } from '../ai/ai-panel-placeholder.component';
import { ProjectKpiSectionComponent } from './project-kpi-section.component';
import { ProjectPhasesSectionComponent } from './project-phases-section.component';
import { ProjectInsightsSectionComponent } from './project-insights-section.component';
import { ProjectReportComparisonComponent } from './project-report-comparison.component';

type LoadStatus = 'loading' | 'success' | 'error';

@Component({
  selector: 'app-project-detail-page',
  imports: [
    RouterLink,
    BreadcrumbsComponent,
    FactsAiLayoutComponent,
    StatusBadgeComponent,
    AiPanelPlaceholderComponent,
    ProjectKpiSectionComponent,
    ProjectPhasesSectionComponent,
    ProjectInsightsSectionComponent,
    ProjectReportComparisonComponent,
  ],
  templateUrl: './project-detail-page.component.html',
  styleUrl: './project-detail-page.component.scss',
})
export class ProjectDetailPageComponent {
  private readonly projectApi = inject(ProjectApiService);

  readonly id = input.required<string>();

  readonly masterData = signal<ProjectMasterData | null>(null);
  readonly masterDataStatus = signal<LoadStatus>('loading');
  readonly masterDataError = signal<string | null>(null);

  readonly projectName = computed(() => this.masterData()?.name ?? `Projekt ${this.id()}`);

  readonly breadcrumbs = computed(() => [
    { label: 'Portfolio', route: ['/portfolio'] as string[] },
    { label: this.projectName() },
  ]);

  constructor() {
    effect(() => {
      this.loadMasterData(this.id());
    });
  }

  loadMasterData(projectId = this.id()): void {
    this.masterDataStatus.set('loading');
    this.masterDataError.set(null);

    this.projectApi
      .getProjectMasterData(projectId)
      .pipe(take(1))
      .subscribe({
        next: (masterData) => {
          this.masterData.set(masterData);
          this.masterDataStatus.set('success');
        },
        error: (error: unknown) => {
          this.masterData.set(null);
          this.masterDataError.set(this.resolveErrorMessage(error));
          this.masterDataStatus.set('error');
        },
      });
  }

  formatDate(value: string | null): string {
    if (!value) {
      return 'Nicht verfügbar';
    }
    return new Intl.DateTimeFormat('de-DE').format(new Date(value));
  }

  private resolveErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const body = error.error as { message?: string } | null;
      if (body?.message) {
        return body.message;
      }
      if (error.status === 0) {
        return 'Die Projekt-Stammdaten konnten nicht geladen werden. Bitte prüfen Sie die Verbindung zum Backend.';
      }
    }
    return 'Die Projekt-Stammdaten konnten nicht geladen werden. Bitte versuchen Sie es erneut.';
  }
}
