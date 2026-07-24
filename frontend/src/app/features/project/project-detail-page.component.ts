import { Component, computed, effect, inject, input, signal, viewChild, ElementRef, HostListener } from '@angular/core';
import { CdkTrapFocus } from '@angular/cdk/a11y';
import { DOCUMENT } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { take } from 'rxjs';

import { ProjectApiService } from '../../core/api/project-api.service';
import { AuthService } from '../../core/auth/auth.service';
import { BreadcrumbsComponent } from '../../core/navigation/breadcrumbs.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';
import { ProjectMasterData } from '../../shared/models/project-detail.model';
import { ProjectIssuesActionsSectionComponent } from './project-issues-actions-section.component';
import { ProjectKpiSectionComponent } from './project-kpi-section.component';
import { ProjectPhasesSectionComponent } from './project-phases-section.component';
import { ProjectReportComparisonComponent } from './project-report-comparison.component';
import { ProjectTeamCapacitySectionComponent } from './project-team-capacity-section.component';
import { ProjectAiPanelComponent } from './project-ai-panel.component';

type LoadStatus = 'loading' | 'success' | 'error';

@Component({
  selector: 'app-project-detail-page',
  imports: [
    RouterLink,
    BreadcrumbsComponent,
    StatusBadgeComponent,
    ProjectKpiSectionComponent,
    ProjectIssuesActionsSectionComponent,
    ProjectTeamCapacitySectionComponent,
    ProjectPhasesSectionComponent,
    ProjectReportComparisonComponent,
    CdkTrapFocus,
    ProjectAiPanelComponent,
  ],
  templateUrl: './project-detail-page.component.html',
  styleUrl: './project-detail-page.component.scss',
})
export class ProjectDetailPageComponent {
  private readonly projectApi = inject(ProjectApiService);
  readonly authService = inject(AuthService);

  readonly id = input.required<string>();

  readonly masterData = signal<ProjectMasterData | null>(null);
  readonly masterDataStatus = signal<LoadStatus>('loading');
  readonly masterDataError = signal<string | null>(null);

  private readonly document = inject(DOCUMENT);
  private readonly launcher = viewChild<ElementRef<HTMLButtonElement>>('aiLauncher');

  readonly projectAiOpen = signal(false);
  private previousBodyOverflow = '';

  openProjectAi(): void {
    this.previousBodyOverflow = this.document.body.style.overflow;
    this.document.body.style.overflow = 'hidden';
    this.projectAiOpen.set(true);
  }

  closeProjectAi(): void {
    if (!this.projectAiOpen()) {
      return;
    }

    this.projectAiOpen.set(false);
    this.document.body.style.overflow = this.previousBodyOverflow;
    this.launcher()?.nativeElement.focus();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.closeProjectAi();
  }

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
      return 'Nicht in den Projektdaten hinterlegt';
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
