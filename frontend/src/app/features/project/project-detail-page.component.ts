import { Component, computed, effect, inject, input, signal, viewChild, ElementRef, HostListener } from '@angular/core';
import { CdkTrapFocus } from '@angular/cdk/a11y';
import { DOCUMENT } from '@angular/common';
import { RouterLink } from '@angular/router';
import { take } from 'rxjs';

import { ProjectApiService } from '../../core/api/project-api.service';
import { AuthService } from '../../core/auth/auth.service';
import { ActiveProjectNavService } from '../../core/navigation/active-project-nav.service';
import { BreadcrumbsComponent } from '../../core/navigation/breadcrumbs.component';
import { StatusBadgeComponent } from '../../shared/components/status-badge.component';
import { ProjectMasterData } from '../../shared/models/project-detail.model';
import { ProjectIssuesActionsSectionComponent } from './project-issues-actions-section.component';
import { ProjectKpiSectionComponent } from './project-kpi-section.component';
import { ProjectPhasesSectionComponent } from './project-phases-section.component';
import { ProjectReportComparisonComponent } from './project-report-comparison.component';
import { ProjectIsoManagementSectionComponent } from './project-iso-management-section.component';
import { ProjectAgileDeliverySectionComponent } from './project-agile-delivery-section.component';
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
    ProjectIsoManagementSectionComponent,
    ProjectAgileDeliverySectionComponent,
    CdkTrapFocus,
    ProjectAiPanelComponent,
  ],
  templateUrl: './project-detail-page.component.html',
  styleUrl: './project-detail-page.component.scss',
})
export class ProjectDetailPageComponent {
  private readonly projectApi = inject(ProjectApiService);
  private readonly activeProjectNav = inject(ActiveProjectNavService);
  readonly authService = inject(AuthService);

  readonly id = input.required<string>();

  readonly masterData = signal<ProjectMasterData | null>(null);
  readonly masterDataStatus = signal<LoadStatus>('loading');
  readonly masterDataError = signal<string | null>(null);
  private masterDataGeneration = 0;

  private readonly document = inject(DOCUMENT);
  private readonly launcher = viewChild<ElementRef<HTMLButtonElement>>('aiLauncher');

  readonly projectAiOpen = signal(false);
  readonly projectAiPanelMounted = signal(false);
  private previousBodyOverflow = '';

  openProjectAi(): void {
    this.projectAiPanelMounted.set(true);
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
  readonly showsAgileDelivery = computed(() => {
    const method = this.masterData()?.deliveryMethod;
    return method === 'AGILE' || method === 'HYBRID';
  });

  readonly breadcrumbs = computed(() => [
    { label: 'Portfolio', route: ['/portfolio'] as string[] },
    { label: this.projectName() },
  ]);

  constructor() {
    effect(() => {
      const projectId = this.id();
      const identity = this.currentIdentity();
      if (!identity) {
        this.masterDataGeneration++;
        this.masterData.set(null);
        return;
      }
      this.loadMasterData(projectId, identity);
    });
  }

  loadMasterData(projectId = this.id(), expectedIdentity = this.currentIdentity()): void {
    if (!expectedIdentity) {
      return;
    }
    const generation = ++this.masterDataGeneration;
    this.masterDataStatus.set('loading');
    this.masterDataError.set(null);

    this.projectApi
      .getProjectMasterData(projectId)
      .pipe(take(1))
      .subscribe({
        next: (masterData) => {
          if (
            generation !== this.masterDataGeneration ||
            projectId !== this.id() ||
            expectedIdentity !== this.currentIdentity()
          ) {
            return;
          }
          this.masterData.set(masterData);
          this.masterDataStatus.set('success');
          this.activeProjectNav.setActiveProject(masterData.id, masterData.name);
        },
        error: (error: unknown) => {
          if (
            generation !== this.masterDataGeneration ||
            projectId !== this.id() ||
            expectedIdentity !== this.currentIdentity()
          ) {
            return;
          }
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

  private resolveErrorMessage(_error: unknown): string {
    return 'Die Projekt-Stammdaten konnten nicht geladen werden. Bitte versuchen Sie es erneut.';
  }

  private currentIdentity(): string | null {
    const user = this.authService.currentUser();
    return user ? `${user.workspaceId}:${user.userId}` : null;
  }
}
