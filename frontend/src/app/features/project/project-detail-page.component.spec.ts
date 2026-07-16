import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { ProjectDetailPageComponent } from './project-detail-page.component';

describe('ProjectDetailPageComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectDetailPageComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should show breadcrumb and project master data from the API (Story 6.2)', () => {
    const fixture = TestBed.createComponent(ProjectDetailPageComponent);
    fixture.componentRef.setInput('id', 'a0000000-0000-4000-8000-000000000001');
    fixture.detectChanges();

    const request = httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/master-data');
    request.flush({
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

    const kpiRequest = httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/kpis');
    kpiRequest.flush({
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

    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/insights').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      insights: [],
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
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Portfolio');
    expect(text).toContain('Nexus Analytics Pilot');
    expect(text).toContain('Acme Fabrications GmbH');
    expect(text).toContain('Mara Neumann');
    expect(text).toContain('Rollout & Betrieb');
    expect(text).toContain('Auf Kurs');
    expect(text).toContain('Zurück zum Portfolio');
    expect(fixture.nativeElement.querySelector('a.page__back')?.getAttribute('href')).toBe(
      '/portfolio',
    );
  });
});
