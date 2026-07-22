import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { ProjectPhasesSectionComponent } from './project-phases-section.component';

describe('ProjectPhasesSectionComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectPhasesSectionComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should render project schedule heading, gantt markers and milestone cards (Story 6.4)', () => {
    const fixture = TestBed.createComponent(ProjectPhasesSectionComponent);
    fixture.componentRef.setInput('projectId', 'a0000000-0000-4000-8000-000000000001');
    fixture.detectChanges();

    httpMock.expectOne('/api/projects/a0000000-0000-4000-8000-000000000001/phases').flush({
      projectId: 'a0000000-0000-4000-8000-000000000001',
      projectName: 'Nexus Analytics Pilot',
      startDate: '2025-03-01',
      plannedEndDate: '2026-06-30',
      forecastEndDate: '2026-07-15',
      actualEndDate: null,
      scheduleDeviationDays: 15,
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      phases: [
        {
          name: 'Umsetzung',
          phaseType: 'IMPLEMENTATION',
          status: 'IN_PROGRESS',
          statusLabel: 'Laufend',
          plannedStartDate: '2025-06-01',
          plannedEndDate: '2026-03-31',
          actualOrForecastStartDate: null,
          actualOrForecastEndDate: null,
          deviationDays: null,
          blockers: null,
          sortOrder: 2,
        },
      ],
      milestones: [
        {
          name: 'Pilot-Release',
          status: 'OVERDUE',
          statusLabel: 'Überfällig',
          plannedDueDate: '2026-06-30',
          actualOrForecastDate: null,
          deviationDays: null,
          overdueDays: 1,
          overdue: true,
          blockers: null,
        },
        {
          name: 'Go-Live',
          status: 'PLANNED',
          statusLabel: 'Geplant',
          plannedDueDate: '2026-09-01',
          actualOrForecastDate: null,
          deviationDays: null,
          overdueDays: null,
          overdue: false,
          blockers: null,
        },
      ],
      accessibilitySummary: 'Phasen: Umsetzung. Überfällige Meilensteine: Pilot-Release.',
    });
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Projektzeitplan & Meilensteine');
    expect(text).not.toContain('Portfolio-Zeitleiste');
    expect(text).not.toContain('Terminlage');
    expect(text).toContain('Planende');
    expect(text).toContain('Forecast-Ende');
    expect(text).toContain('Heute');
    expect(text).toContain('Pilot-Release');
    expect(text).toContain('Überfällig');
    expect(text).toContain('Go-Live');
    expect(text).toContain('Überfällig seit 1 Tag');
    expect(text).not.toContain('Abweichung');
    expect(text).not.toContain('+1 Tag');
    expect(fixture.nativeElement.querySelectorAll('.milestone-card').length).toBe(2);
    expect(fixture.nativeElement.querySelector('.milestone-card--overdue')).toBeTruthy();
    expect(text).toContain('Überfällige Meilensteine: Pilot-Release.');
  });
});
