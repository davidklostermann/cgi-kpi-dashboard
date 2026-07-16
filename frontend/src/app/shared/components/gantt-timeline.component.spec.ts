import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GanttTimelineComponent } from './gantt-timeline.component';
import { PortfolioTimelineProject } from '../models/portfolio-timeline.model';

const mockProjects: PortfolioTimelineProject[] = [
  {
    id: 'a0000000-0000-4000-8000-000000000001',
    name: 'Nexus Analytics Pilot',
    startDate: '2025-03-01',
    plannedEndDate: '2026-06-30',
    forecastEndDate: '2026-07-15',
    actualEndDate: null,
    scheduleDeviationDays: 15,
    status: 'AT_RISK',
    statusLabel: 'Beobachten',
    phases: [
      {
        name: 'Umsetzung',
        phaseType: 'UMSETZUNG',
        startDate: '2025-09-01',
        endDate: '2026-06-30',
        sortOrder: 2,
      },
    ],
    milestones: [
      {
        name: 'Pilot-Release',
        dueDate: '2026-06-30',
        completedDate: null,
        status: 'AT_RISK',
        statusLabel: 'Beobachten',
      },
    ],
  },
  {
    id: 'a0000000-0000-4000-8000-000000000002',
    name: 'On-Track Project',
    startDate: '2025-06-01',
    plannedEndDate: '2026-03-31',
    forecastEndDate: '2026-03-31',
    actualEndDate: null,
    scheduleDeviationDays: 0,
    status: 'ON_TRACK',
    statusLabel: 'Auf Kurs',
    phases: [],
    milestones: [],
  },
];

describe('GanttTimelineComponent', () => {
  let fixture: ComponentFixture<GanttTimelineComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GanttTimelineComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(GanttTimelineComponent);
    fixture.componentRef.setInput('projects', mockProjects);
    fixture.detectChanges();
  });

  it('should render one aligned row per project (Story 5.2)', () => {
    expect(
      fixture.nativeElement.querySelectorAll('.gantt-timeline__row').length,
    ).toBe(2);
    expect(
      fixture.nativeElement.querySelectorAll('.gantt-timeline__name-cell')
        .length,
    ).toBe(2);
    expect(fixture.nativeElement.textContent).toContain(
      'Nexus Analytics Pilot',
    );
  });

  it('should expose accessible handover summary', () => {
    const summary = fixture.nativeElement.querySelector(
      '#gantt-timeline-summary',
    ) as HTMLElement;
    expect(summary.classList.contains('visually-hidden')).toBe(true);
    expect(summary.textContent).toContain('geplante Kundenübergabe');
    expect(summary.textContent).toContain('15 Tage Verzug');
  });

  it('should provide keyboard-focusable frame region', () => {
    const frame = fixture.nativeElement.querySelector(
      '.gantt-timeline__frame',
    ) as HTMLElement;
    expect(frame.getAttribute('tabindex')).toBe('0');
    expect(frame.getAttribute('role')).toBe('region');
  });

  it('should render one continuous today marker and a legend', () => {
    expect(
      fixture.nativeElement.querySelector('.gantt-timeline__legend'),
    ).toBeTruthy();
    expect(
      fixture.nativeElement.querySelectorAll('.gantt-timeline__today').length,
    ).toBeLessThanOrEqual(1);
  });

  it('should not render milestone diamonds', () => {
    expect(
      fixture.nativeElement.querySelector('.gantt-timeline__milestone'),
    ).toBeNull();
  });

  it('should display Kundenübergabe and delayed plan marker', () => {
    expect(fixture.nativeElement.textContent).toContain('Kundenübergabe');
    expect(fixture.nativeElement.textContent).toContain('Plan');
    expect(fixture.nativeElement.textContent).toContain('+15 Tage');
  });

  it('should render sticky two-level year and month header', () => {
    expect(
      fixture.nativeElement.querySelectorAll('.gantt-timeline__month').length,
    ).toBeGreaterThan(6);
    expect(
      fixture.nativeElement.querySelector('.gantt-timeline__year'),
    ).toBeTruthy();
    expect(
      getComputedStyle(
        fixture.nativeElement.querySelector('.gantt-timeline__head'),
      ).position,
    ).toBe('sticky');
  });
});
