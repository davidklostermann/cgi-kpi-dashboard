import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { ProjectKpiSectionComponent } from './project-kpi-section.component';

describe('ProjectKpiSectionComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectKpiSectionComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should render Plan/Ist/Prognose KPI cards and budget table (Story 6.3)', () => {
    const fixture = TestBed.createComponent(ProjectKpiSectionComponent);
    fixture.componentRef.setInput('projectId', 'a0000000-0000-4000-8000-000000000001');
    fixture.detectChanges();

    const request = httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/kpis');
    request.flush({
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
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Management-KPIs');
    expect(text).toContain('Auf Kurs');
    expect(text).toContain('Plan');
    expect(text).toContain('Ist');
    expect(text).toContain('Prognose');
    expect(text).toContain('Hochrechnung');
    expect(text).toContain('Budget');
    expect(text).toContain('Aufwand');
  });
});
