import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { WritableSignal, computed, signal } from '@angular/core';

import { ProjectDetailPageComponent } from './project-detail-page.component';
import { ProjectAiPanelComponent } from './project-ai-panel.component';
import { AuthService } from '../../core/auth/auth.service';
import { ActiveProjectNavService } from '../../core/navigation/active-project-nav.service';
import { AuthUser } from '../../shared/models/auth.model';

describe('ProjectDetailPageComponent', () => {
  let httpMock: HttpTestingController;
  let isAdminSignal: WritableSignal<boolean>;
  let currentUserSignal: WritableSignal<AuthUser | null>;
  const projectId = 'a0000000-0000-4000-8000-000000000001';

  function flushIsoManagement(targetProjectId = projectId): void {
    const requests = httpMock.match(`/api/projects/${targetProjectId}/iso-management`);
    for (const request of requests) {
      request.flush({
        projectId: targetProjectId,
        dataAvailable: false,
        emptyReason: 'Keine ISO-Steuerungsfelder für dieses Projekt hinterlegt.',
        factsAsOf: '2026-07-01T08:00:00Z',
        benefits: null,
        scope: null,
        changeRequests: null,
        quality: null,
        stakeholders: null,
      });
    }
  }

  function flushChildSections(targetProjectId = projectId): void {
    httpMock.expectOne(`/api/projects/${targetProjectId}/kpis`).flush({
      projectId: targetProjectId,
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      progressPercent: 62,
      currentPhaseName: 'Rollout & Betrieb',
      schedule: {
        timeElapsedPercent: 80.5,
        deviationDays: 0,
        plannedEndDate: '2026-06-30',
        forecastEndDate: '2026-06-30',
        actualEndDate: null,
      },
      budget: {
        planned: 500000,
        actual: 475000,
        utilizationPercent: 95,
        deviationPercent: -5,
        remaining: 25000,
        forecastAtCompletion: 766129.03,
      },
      effort: {
        plannedDays: 120,
        actualDays: 108,
        deviationPercent: -10,
        remainingDays: 12,
        forecastAtCompletionDays: 174.19,
      },
      risks: { openCount: 0, criticalOpenCount: 0 },
      problems: { openCount: 0, criticalOpenCount: 0 },
    });
    httpMock.expectOne(`/api/projects/${targetProjectId}/phases`).flush({
      projectId: targetProjectId,
      projectName: 'Nexus Analytics Pilot',
      startDate: '2025-03-01',
      plannedEndDate: '2026-06-30',
      forecastEndDate: '2026-06-30',
      actualEndDate: null,
      scheduleDeviationDays: 0,
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      phases: [],
      milestones: [],
      accessibilitySummary: 'Phasen: keine. Keine überfälligen Meilensteine.',
    });
    httpMock.expectOne(`/api/projects/${targetProjectId}/trends`).flush({
      projectId: targetProjectId,
      comparisonAvailable: false,
      unavailableReason: null,
      previousSnapshotDate: null,
      currentSnapshotDate: null,
      progressDeltaPercent: null,
      budgetActualDelta: null,
      scheduleDeviationDeltaDays: null,
      previousStatus: null,
      previousStatusLabel: null,
      currentStatus: 'ON_TRACK',
      currentStatusLabel: 'Auf Kurs',
      openRiskCountDelta: null,
    });
    httpMock.expectOne(`/api/projects/${targetProjectId}/issues-actions`).flush({
      projectId: targetProjectId,
      factsBadge: 'Datenstand 10.07.2026',
      factsAsOf: '2026-07-01T08:00:00Z',
      items: [],
    });
    httpMock.expectOne(`/api/projects/${targetProjectId}/capacity`).flush({
      projectId: targetProjectId,
      factsAsOf: '2026-07-10T08:00:00Z',
      factsBadge: 'Datenstand 10.07.2026',
      roles: [],
      summary: null,
    });
    flushIsoManagement(targetProjectId);
  }

  beforeEach(async () => {
    isAdminSignal = signal(false);
    currentUserSignal = signal({
      userId: 'user-a',
      workspaceId: 'workspace-a',
      username: 'admin',
      roles: ['ROLE_ADMIN'],
      mustChangePassword: false,
    });
    const authServiceMock = {
      currentUser: currentUserSignal,
      isAdmin: computed(() => isAdminSignal()),
    };

    await TestBed.configureTestingModule({
      imports: [ProjectDetailPageComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceMock },
      ],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should show breadcrumb and project master data from the API (Story 6.2)', () => {
    isAdminSignal.set(true);
    const fixture = TestBed.createComponent(ProjectDetailPageComponent);
    fixture.componentRef.setInput('id', 'a0000000-0000-4000-8000-000000000001');
    fixture.detectChanges();

    const masterDataRequest = httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/master-data');
    masterDataRequest.flush({
      id: 'a0000000-0000-4000-8000-000000000001',
      name: 'Nexus Analytics Pilot',
      customer: 'Acme Fabrications GmbH',
      projectLead: 'Mara Neumann',
      startDate: '2025-03-01',
      plannedEndDate: '2026-06-30',
      forecastEndDate: '2026-06-30',
      currentPhaseName: 'Rollout & Betrieb',
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      lastDataUpdate: '2026-07-01T08:00:00Z',
    });

    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/kpis').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      progressPercent: 62,
      currentPhaseName: 'Rollout & Betrieb',
      schedule: {
        timeElapsedPercent: 80.5,
        deviationDays: 0,
        plannedEndDate: '2026-06-30',
        forecastEndDate: '2026-06-30',
        actualEndDate: null,
      },
      budget: {
        planned: 500000,
        actual: 475000,
        utilizationPercent: 95,
        deviationPercent: -5,
        remaining: 25000,
        forecastAtCompletion: 766129.03,
      },
      effort: {
        plannedDays: 120,
        actualDays: 108,
        deviationPercent: -10,
        remainingDays: 12,
        forecastAtCompletionDays: 174.19,
      },
      risks: { openCount: 0, criticalOpenCount: 0 },
      problems: { openCount: 0, criticalOpenCount: 0 },
    });

    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/phases').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      projectName: 'Nexus Analytics Pilot',
      startDate: '2025-03-01',
      plannedEndDate: '2026-06-30',
      forecastEndDate: '2026-06-30',
      actualEndDate: null,
      scheduleDeviationDays: 0,
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      phases: [],
      milestones: [],
      accessibilitySummary: 'Phasen: keine. Keine überfälligen Meilensteine.',
    });

    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/trends').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      comparisonAvailable: true,
      unavailableReason: null,
      previousSnapshotDate: '2026-06-01',
      currentSnapshotDate: '2026-07-01',
      progressDeltaPercent: 2,
      budgetActualDelta: 1000,
      scheduleDeviationDeltaDays: 0,
      previousStatus: 'ON_TRACK',
      previousStatusLabel: 'Auf Kurs',
      currentStatus: 'ON_TRACK',
      currentStatusLabel: 'Auf Kurs',
      openRiskCountDelta: 0,
    });

    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/issues-actions').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      factsBadge: 'Datenstand 10.07.2026',
      factsAsOf: '2026-07-01T08:00:00Z',
      items: [],
    });

    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/capacity').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      factsAsOf: '2026-07-10T08:00:00Z',
      factsBadge: 'Datenstand 10.07.2026',
      roles: [],
      summary: null,
    });

    flushIsoManagement();
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Portfolio');
    expect(text).toContain('Nexus Analytics Pilot');
    expect(text).toContain('Acme Fabrications GmbH');
    expect(text).toContain('Mara Neumann');
    expect(text).toContain('Rollout & Betrieb');
    expect(text).toContain('Auf Kurs');
    expect(text).toContain('Zurück zum Portfolio');
    expect(text).not.toContain('Testzusammenfassung'); // No AI analysis initially
    expect(fixture.nativeElement.querySelector('a.page__back')?.getAttribute('href')).toBe(
      '/portfolio',
    );
    expect(fixture.nativeElement.querySelector('.portfolio-ai-launcher')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.portfolio-ai-drawer')).toBeNull();
  });

  it('ignores a master-data response that arrives after logout', () => {
    const fixture = TestBed.createComponent(ProjectDetailPageComponent);
    fixture.componentRef.setInput('id', projectId);
    fixture.detectChanges();

    const masterRequest = httpMock.expectOne(`/api/projects/${projectId}/master-data`);
    for (const request of httpMock.match((candidate) => !candidate.url.endsWith('/master-data'))) {
      request.flush({ message: 'request closed for test' }, { status: 500, statusText: 'Error' });
    }

    currentUserSignal.set(null);
    fixture.detectChanges();
    masterRequest.flush({
      id: projectId,
      name: 'Projekt des vorherigen Kontos',
      customer: 'Acme',
      projectLead: 'Mara Neumann',
      startDate: '2025-03-01',
      plannedEndDate: '2026-06-30',
      forecastEndDate: '2026-06-30',
      currentPhaseName: 'Rollout',
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      lastDataUpdate: '2026-07-01T08:00:00Z',
    });
    fixture.detectChanges();

    expect(fixture.componentInstance.masterData()).toBeNull();
    expect(TestBed.inject(ActiveProjectNavService).activeProject()).toBeNull();
  });

  it('should show the AI launcher for ADMIN users and open/close the drawer', () => {
    isAdminSignal.set(true);
    const fixture = TestBed.createComponent(ProjectDetailPageComponent);
    fixture.componentRef.setInput('id', 'a0000000-0000-4000-8000-000000000001');
    fixture.detectChanges();

    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/master-data').flush({
      id: 'a0000000-0000-4000-8000-000000000001',
      name: 'Nexus Analytics Pilot',
      customer: 'Acme Fabrications GmbH',
      projectLead: 'Mara Neumann',
      startDate: '2025-03-01',
      plannedEndDate: '2026-06-30',
      forecastEndDate: '2026-06-30',
      currentPhaseName: 'Rollout & Betrieb',
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      lastDataUpdate: '2026-07-01T08:00:00Z',
    });
    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/kpis').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      progressPercent: 62,
      currentPhaseName: 'Rollout & Betrieb',
      schedule: {
        timeElapsedPercent: 80.5,
        deviationDays: 0,
        plannedEndDate: '2026-06-30',
        forecastEndDate: '2026-06-30',
        actualEndDate: null,
      },
      budget: {
        planned: 500000,
        actual: 475000,
        utilizationPercent: 95,
        deviationPercent: -5,
        remaining: 25000,
        forecastAtCompletion: 766129.03,
      },
      effort: {
        plannedDays: 120,
        actualDays: 108,
        deviationPercent: -10,
        remainingDays: 12,
        forecastAtCompletionDays: 174.19,
      },
      risks: { openCount: 0, criticalOpenCount: 0 },
      problems: { openCount: 0, criticalOpenCount: 0 },
    });
    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/phases').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      projectName: 'Nexus Analytics Pilot',
      startDate: '2025-03-01',
      plannedEndDate: '2026-06-30',
      forecastEndDate: '2026-06-30',
      actualEndDate: null,
      scheduleDeviationDays: 0,
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      phases: [],
      milestones: [],
      accessibilitySummary: 'Phasen: keine. Keine überfälligen Meilensteine.',
    });
    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/trends').flush({});
    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/issues-actions').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      factsBadge: 'Datenstand 10.07.2026',
      factsAsOf: '2026-07-01T08:00:00Z',
      items: [],
    });
    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/capacity').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      factsAsOf: '2026-07-10T08:00:00Z',
      factsBadge: 'Datenstand 10.07.2026',
      roles: [],
      summary: null,
    });
    flushIsoManagement();
    fixture.detectChanges();

    const launcher = fixture.nativeElement.querySelector('.portfolio-ai-launcher') as HTMLButtonElement;
    expect(launcher).toBeTruthy();

    launcher.click();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.portfolio-ai-drawer-backdrop--open')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('app-project-ai-panel')).toBeTruthy();
    expect(document.body.style.overflow).toBe('hidden');

    const closeButton = fixture.nativeElement.querySelector('.portfolio-ai-drawer__close') as HTMLButtonElement;
    closeButton.click();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.portfolio-ai-drawer-backdrop--open')).toBeNull();
    expect(fixture.nativeElement.querySelector('app-project-ai-panel')).toBeTruthy();
    expect(document.activeElement).toBe(launcher);
    expect(document.body.style.overflow).toBe('');
  });

  it('should keep loaded AI analysis when the drawer is closed and reopened', () => {
    isAdminSignal.set(true);
    const fixture = TestBed.createComponent(ProjectDetailPageComponent);
    fixture.componentRef.setInput('id', 'a0000000-0000-4000-8000-000000000001');
    fixture.detectChanges();

    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/master-data').flush({
      id: 'a0000000-0000-4000-8000-000000000001',
      name: 'Nexus Analytics Pilot',
      customer: 'Acme Fabrications GmbH',
      projectLead: 'Mara Neumann',
      startDate: '2025-03-01',
      plannedEndDate: '2026-06-30',
      forecastEndDate: '2026-06-30',
      currentPhaseName: 'Rollout & Betrieb',
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      lastDataUpdate: '2026-07-01T08:00:00Z',
    });
    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/kpis').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      progressPercent: 62,
      currentPhaseName: 'Rollout & Betrieb',
      schedule: {
        timeElapsedPercent: 80.5,
        deviationDays: 0,
        plannedEndDate: '2026-06-30',
        forecastEndDate: '2026-06-30',
        actualEndDate: null,
      },
      budget: {
        planned: 500000,
        actual: 475000,
        utilizationPercent: 95,
        deviationPercent: -5,
        remaining: 25000,
        forecastAtCompletion: 766129.03,
      },
      effort: {
        plannedDays: 120,
        actualDays: 108,
        deviationPercent: -10,
        remainingDays: 12,
        forecastAtCompletionDays: 174.19,
      },
      risks: { openCount: 0, criticalOpenCount: 0 },
      problems: { openCount: 0, criticalOpenCount: 0 },
    });
    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/phases').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      projectName: 'Nexus Analytics Pilot',
      startDate: '2025-03-01',
      plannedEndDate: '2026-06-30',
      forecastEndDate: '2026-06-30',
      actualEndDate: null,
      scheduleDeviationDays: 0,
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      phases: [],
      milestones: [],
      accessibilitySummary: 'Phasen: keine. Keine überfälligen Meilensteine.',
    });
    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/trends').flush({});
    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/issues-actions').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      factsBadge: 'Datenstand 10.07.2026',
      factsAsOf: '2026-07-01T08:00:00Z',
      items: [],
    });
    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/capacity').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      factsAsOf: '2026-07-10T08:00:00Z',
      factsBadge: 'Datenstand 10.07.2026',
      roles: [],
      summary: null,
    });
    flushIsoManagement();
    fixture.detectChanges();

    const launcher = fixture.nativeElement.querySelector('.portfolio-ai-launcher') as HTMLButtonElement;
    launcher.click();
    fixture.detectChanges();

    const panel = fixture.debugElement.query(
      (element) => element.name === 'app-project-ai-panel',
    )?.componentInstance as ProjectAiPanelComponent;
    panel.loadAnalysis(false);
    fixture.detectChanges();

    httpMock.expectOne('/api/me/ai/readiness').flush({ ready: true });
    httpMock
      .expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/ai/analysis?refresh=false')
      .flush({
        projectId: 'a0000000-0000-4000-8000-000000000001',
        factsAsOf: '2026-07-01T08:00:00Z',
        generatedAt: '2026-07-16T12:00:00Z',
        status: 'SUCCESS',
        availableSources: ['KPI'],
        summary: 'Managementbewertung bleibt sichtbar.',
        priorities: [],
        suggestedActions: [],
        missingData: [],
        aiGenerated: true,
        disclaimer: 'Disclaimer',
      });
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Managementbewertung bleibt sichtbar.');

    const closeButton = fixture.nativeElement.querySelector('.portfolio-ai-drawer__close') as HTMLButtonElement;
    closeButton.click();
    fixture.detectChanges();

    launcher.click();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Managementbewertung bleibt sichtbar.');
    expect(fixture.nativeElement.textContent).not.toContain('Projekt analysieren');
    httpMock.expectNone('/api/projects/a0000000-0000-4000-8000-000000000001/ai/analysis?refresh=false');
  });

  it('should hide the AI panel for USER users and not make AI API calls', () => {
    isAdminSignal.set(false);
    const fixture = TestBed.createComponent(ProjectDetailPageComponent);
    fixture.componentRef.setInput('id', 'a0000000-0000-4000-8000-000000000001');
    fixture.detectChanges();

    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/master-data').flush({
      id: 'a0000000-0000-4000-8000-000000000001',
      name: 'Nexus Analytics Pilot',
      customer: 'Acme Fabrications GmbH',
      projectLead: 'Mara Neumann',
      startDate: '2025-03-01',
      plannedEndDate: '2026-06-30',
      forecastEndDate: '2026-06-30',
      currentPhaseName: 'Rollout & Betrieb',
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      lastDataUpdate: '2026-07-01T08:00:00Z',
    });
    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/kpis').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      progressPercent: 62,
      currentPhaseName: 'Rollout & Betrieb',
      schedule: {
        timeElapsedPercent: 80.5,
        deviationDays: 0,
        plannedEndDate: '2026-06-30',
        forecastEndDate: '2026-06-30',
        actualEndDate: null,
      },
      budget: {
        planned: 500000,
        actual: 475000,
        utilizationPercent: 95,
        deviationPercent: -5,
        remaining: 25000,
        forecastAtCompletion: 766129.03,
      },
      effort: {
        plannedDays: 120,
        actualDays: 108,
        deviationPercent: -10,
        remainingDays: 12,
        forecastAtCompletionDays: 174.19,
      },
      risks: { openCount: 0, criticalOpenCount: 0 },
      problems: { openCount: 0, criticalOpenCount: 0 },
    });
    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/phases').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      projectName: 'Nexus Analytics Pilot',
      startDate: '2025-03-01',
      plannedEndDate: '2026-06-30',
      forecastEndDate: '2026-06-30',
      actualEndDate: null,
      scheduleDeviationDays: 0,
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      phases: [],
      milestones: [],
      accessibilitySummary: 'Phasen: keine. Keine überfälligen Meilensteine.',
    });
    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/trends').flush({});
    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/issues-actions').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      factsBadge: 'Datenstand 10.07.2026',
      factsAsOf: '2026-07-01T08:00:00Z',
      items: [],
    });
    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/capacity').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      factsAsOf: '2026-07-10T08:00:00Z',
      factsBadge: 'Datenstand 10.07.2026',
      roles: [],
      summary: null,
    });

    flushIsoManagement();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.portfolio-ai-launcher')).toBeNull();
    expect(fixture.nativeElement.querySelector('.portfolio-ai-drawer')).toBeNull();
    httpMock.expectNone('/api/projects/a0000000-0000-4000-8000-000000000001/ai/analysis?refresh=false');
  });

  it('should hide the AI launcher when no authenticated user is available', () => {
    isAdminSignal.set(false);
    const fixture = TestBed.createComponent(ProjectDetailPageComponent);
    fixture.componentRef.setInput('id', 'a0000000-0000-4000-8000-000000000001');
    fixture.detectChanges();

    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/master-data').flush({
      id: 'a0000000-0000-4000-8000-000000000001',
      name: 'Nexus Analytics Pilot',
      customer: 'Acme Fabrications GmbH',
      projectLead: 'Mara Neumann',
      startDate: '2025-03-01',
      plannedEndDate: '2026-06-30',
      forecastEndDate: '2026-06-30',
      currentPhaseName: 'Rollout & Betrieb',
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      lastDataUpdate: '2026-07-01T08:00:00Z',
    });
    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/kpis').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      progressPercent: 62,
      currentPhaseName: 'Rollout & Betrieb',
      schedule: {
        timeElapsedPercent: 80.5,
        deviationDays: 0,
        plannedEndDate: '2026-06-30',
        forecastEndDate: '2026-06-30',
        actualEndDate: null,
      },
      budget: {
        planned: 500000,
        actual: 475000,
        utilizationPercent: 95,
        deviationPercent: -5,
        remaining: 25000,
        forecastAtCompletion: 766129.03,
      },
      effort: {
        plannedDays: 120,
        actualDays: 108,
        deviationPercent: -10,
        remainingDays: 12,
        forecastAtCompletionDays: 174.19,
      },
      risks: { openCount: 0, criticalOpenCount: 0 },
      problems: { openCount: 0, criticalOpenCount: 0 },
    });
    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/phases').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      projectName: 'Nexus Analytics Pilot',
      startDate: '2025-03-01',
      plannedEndDate: '2026-06-30',
      forecastEndDate: '2026-06-30',
      actualEndDate: null,
      scheduleDeviationDays: 0,
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      phases: [],
      milestones: [],
      accessibilitySummary: 'Phasen: keine. Keine überfälligen Meilensteine.',
    });
    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/trends').flush({});
    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/issues-actions').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      factsBadge: 'Datenstand 10.07.2026',
      factsAsOf: '2026-07-01T08:00:00Z',
      items: [],
    });
    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/capacity').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      factsAsOf: '2026-07-10T08:00:00Z',
      factsBadge: 'Datenstand 10.07.2026',
      roles: [],
      summary: null,
    });

    flushIsoManagement();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.portfolio-ai-launcher')).toBeNull();
    expect(fixture.nativeElement.querySelector('.portfolio-ai-drawer')).toBeNull();
    httpMock.expectNone('/api/projects/a0000000-0000-4000-8000-000000000001/ai/analysis?refresh=false');
  });

  it('should hide Agile Delivery for WATERFALL projects (Story 17.3 / 17.4)', () => {
    const fixture = TestBed.createComponent(ProjectDetailPageComponent);
    fixture.componentRef.setInput('id', projectId);
    fixture.detectChanges();

    httpMock.expectOne(`/api/projects/${projectId}/master-data`).flush({
      id: projectId,
      name: 'Nexus Analytics Pilot',
      customer: 'Acme Fabrications GmbH',
      projectLead: 'Mara Neumann',
      startDate: '2025-03-01',
      plannedEndDate: '2026-06-30',
      forecastEndDate: '2026-06-30',
      currentPhaseName: 'Rollout & Betrieb',
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      deliveryMethod: 'WATERFALL',
      deliveryMethodLabel: 'Klassisch',
      lastDataUpdate: '2026-07-01T08:00:00Z',
    });
    flushChildSections(projectId);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('app-project-agile-delivery-section')).toBeNull();
    expect(fixture.nativeElement.textContent).not.toContain('Agile Delivery');
    httpMock.expectNone(`/api/projects/${projectId}/agile-delivery`);
  });

  it('should show Agile Delivery for AGILE projects (Story 17.3 / 17.4)', () => {
    const agileId = 'a0000000-0000-4000-8000-000000000006';
    const fixture = TestBed.createComponent(ProjectDetailPageComponent);
    fixture.componentRef.setInput('id', agileId);
    fixture.detectChanges();

    httpMock.expectOne(`/api/projects/${agileId}/master-data`).flush({
      id: agileId,
      name: 'Agile Pilot',
      customer: 'Acme Fabrications GmbH',
      projectLead: 'Mara Neumann',
      startDate: '2025-03-01',
      plannedEndDate: '2026-06-30',
      forecastEndDate: '2026-06-30',
      currentPhaseName: 'Umsetzung',
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      deliveryMethod: 'AGILE',
      deliveryMethodLabel: 'Agil',
      lastDataUpdate: '2026-07-01T08:00:00Z',
    });
    flushChildSections(agileId);
    fixture.detectChanges();

    httpMock.expectOne(`/api/projects/${agileId}/agile-delivery`).flush({
      projectId: agileId,
      deliveryMethod: 'AGILE',
      dataAvailable: true,
      emptyReason: null,
      dataSource: 'INTERNAL_MOCK',
      factsAsOf: '2026-07-01T08:00:00Z',
      sprints: [
        {
          id: 'a1700000-0000-4000-8000-000000000001',
          name: 'S1',
          sequenceNo: 1,
          lifecycle: 'ACTIVE',
          current: true,
          future: false,
          health: 'WATCH',
          healthLabel: 'Achtung',
          progressPercent: 50,
          storyPointsPlanned: 40,
          storyPointsCompleted: 20,
          carryOverPoints: 3,
          startDate: '2026-01-20',
          endDate: '2026-02-02',
        },
      ],
      chart: {
        sprintLabels: ['S1'],
        plannedStoryPoints: [40],
        completedStoryPoints: [20],
        velocityTrend: [20],
        futureFlags: [false],
      },
      kpis: {
        sprintHealth: 'WATCH',
        sprintHealthLabel: 'Achtung',
        totalStoryPoints: 40,
        averageVelocity: null,
        carryOverNextSprint: 3,
        openBlockerCount: 1,
      },
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('app-project-agile-delivery-section')).toBeTruthy();
    expect(fixture.nativeElement.textContent).toContain('Agile Delivery');
  });
});
