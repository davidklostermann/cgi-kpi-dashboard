import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { WritableSignal, computed, signal } from '@angular/core';

import { ProjectDetailPageComponent } from './project-detail-page.component';
import { AuthService } from '../../core/auth/auth.service';

describe('ProjectDetailPageComponent', () => {
  let httpMock: HttpTestingController;
  let isAdminSignal: WritableSignal<boolean>;

  beforeEach(async () => {
    isAdminSignal = signal(false);
    const authServiceMock = {
      currentUser: signal<{ id: string; roles: string[] } | null>(null),
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
      factsBadge: 'Fakten aus Backend',
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
      factsBadge: 'Fakten aus Backend',
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
    fixture.detectChanges();

    const launcher = fixture.nativeElement.querySelector('.portfolio-ai-launcher') as HTMLButtonElement;
    expect(launcher).toBeTruthy();

    launcher.click();
    fixture.detectChanges();

    httpMock.expectOne('/api/me/ai/readiness').flush({ ready: true });
    const analysisRequest = httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/ai/analysis?refresh=false');
    analysisRequest.flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      factsAsOf: '2026-07-01T08:00:00Z',
      generatedAt: '2026-07-16T12:00:00Z',
      status: 'SUCCESS',
      availableSources: ['KPI'],
      summary: 'Testzusammenfassung',
      priorities: [],
      suggestedActions: [],
      missingData: [],
      aiGenerated: true,
      disclaimer: 'Disclaimer',
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.portfolio-ai-drawer')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('app-project-ai-panel')).toBeTruthy();
    expect(document.body.style.overflow).toBe('hidden');

    const closeButton = fixture.nativeElement.querySelector('.portfolio-ai-drawer__close') as HTMLButtonElement;
    closeButton.click();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.portfolio-ai-drawer')).toBeNull();
    expect(document.activeElement).toBe(launcher);
    expect(document.body.style.overflow).toBe('');
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
      factsBadge: 'Fakten aus Backend',
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
      factsBadge: 'Fakten aus Backend',
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

    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.portfolio-ai-launcher')).toBeNull();
    expect(fixture.nativeElement.querySelector('.portfolio-ai-drawer')).toBeNull();
    httpMock.expectNone('/api/projects/a0000000-0000-4000-8000-000000000001/ai/analysis?refresh=false');
  });
});
