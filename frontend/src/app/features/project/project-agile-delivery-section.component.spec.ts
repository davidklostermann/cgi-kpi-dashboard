import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { ProjectAgileDeliverySectionComponent } from './project-agile-delivery-section.component';

describe('ProjectAgileDeliverySectionComponent', () => {
  let httpMock: HttpTestingController;
  const projectId = 'a0000000-0000-4000-8000-000000000006';

  const payload = {
    projectId,
    deliveryMethod: 'AGILE' as const,
    dataAvailable: true,
    emptyReason: null,
    dataSource: 'INTERNAL_MOCK',
    factsAsOf: '2026-08-04T08:00:00Z',
    sprints: [
      {
        id: 'a1700000-0000-4000-8000-000000000001',
        name: 'S1',
        sequenceNo: 1,
        lifecycle: 'PAST' as const,
        current: false,
        future: false,
        health: 'GOOD' as const,
        healthLabel: 'Gut',
        progressPercent: 95,
        storyPointsPlanned: 40,
        storyPointsCompleted: 38,
        carryOverPoints: 2,
        startDate: '2026-01-06',
        endDate: '2026-01-19',
      },
      {
        id: 'a1700000-0000-4000-8000-000000000002',
        name: 'S2',
        sequenceNo: 2,
        lifecycle: 'ACTIVE' as const,
        current: true,
        future: false,
        health: 'WATCH' as const,
        healthLabel: 'Achtung',
        progressPercent: 50,
        storyPointsPlanned: 40,
        storyPointsCompleted: 20,
        carryOverPoints: 3,
        startDate: '2026-01-20',
        endDate: '2026-02-02',
      },
      {
        id: 'a1700000-0000-4000-8000-000000000003',
        name: 'S3',
        sequenceNo: 3,
        lifecycle: 'FUTURE' as const,
        current: false,
        future: true,
        health: 'PLANNED' as const,
        healthLabel: 'Geplant',
        progressPercent: 0,
        storyPointsPlanned: 45,
        storyPointsCompleted: 0,
        carryOverPoints: 0,
        startDate: '2026-02-03',
        endDate: '2026-02-16',
      },
    ],
    chart: {
      sprintLabels: ['S1', 'S2', 'S3'],
      plannedStoryPoints: [40, 40, 45],
      completedStoryPoints: [38, 20, 0],
      velocityTrend: [38, 20, null],
      futureFlags: [false, false, true],
    },
    kpis: {
      sprintHealth: 'WATCH',
      sprintHealthLabel: 'Achtung',
      totalStoryPoints: 125,
      averageVelocity: 38,
      carryOverNextSprint: 3,
      openBlockerCount: 2,
    },
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectAgileDeliverySectionComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('renders sprint cards, chart, and exact KPI line', () => {
    const fixture = TestBed.createComponent(ProjectAgileDeliverySectionComponent);
    fixture.componentRef.setInput('projectId', projectId);
    fixture.detectChanges();

    httpMock.expectOne(`/api/projects/${projectId}/agile-delivery`).flush(payload);
    fixture.detectChanges();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Agile Delivery');
    expect(text).toContain('Sprint-Übersicht und Fortschritt');
    expect(text).toContain('Sprint-Entwicklung');
    expect(text).toContain('Datenquelle: Internes System (Mock)');
    expect(text).toContain('Sprint Health');
    expect(text).toContain('Gesamte Story Points');
    expect(text).toContain('Ø Velocity');
    expect(text).toContain('Carry-over nächster Sprint');
    expect(text).toContain('Blocker gesamt');
    expect(fixture.nativeElement.querySelectorAll('.agile-delivery__sprint').length).toBe(3);
    expect(
      fixture.nativeElement.querySelector('.agile-delivery__sprint--current'),
    ).toBeTruthy();
    expect(
      fixture.nativeElement.querySelector('.agile-delivery__sprint--future'),
    ).toBeTruthy();
    expect(fixture.nativeElement.querySelector('svg')).toBeTruthy();
    const legendItems = fixture.nativeElement.querySelectorAll(
      '.agile-delivery__legend li',
    ) as NodeListOf<HTMLLIElement>;
    expect(legendItems.length).toBe(3);
    expect(legendItems[0].textContent?.replace(/\s+/g, ' ').trim()).toBe('Geplante SP');
    expect(legendItems[1].textContent?.replace(/\s+/g, ' ').trim()).toBe('Erreichte SP');
    expect(legendItems[2].textContent?.replace(/\s+/g, ' ').trim()).toBe('Velocity-Trend');
  });

  it('renders the empty state for agile projects without sprint data', () => {
    const fixture = TestBed.createComponent(ProjectAgileDeliverySectionComponent);
    fixture.componentRef.setInput('projectId', projectId);
    fixture.detectChanges();

    httpMock.expectOne(`/api/projects/${projectId}/agile-delivery`).flush({
      ...payload,
      dataAvailable: false,
      emptyReason: 'Keine Sprint-Daten für dieses Projekt hinterlegt.',
      sprints: [],
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain(
      'Keine Sprint-Daten für dieses Projekt hinterlegt.',
    );
    expect(fixture.nativeElement.querySelector('svg')).toBeNull();
  });

  it('renders an error and retries the independent request', () => {
    const fixture = TestBed.createComponent(ProjectAgileDeliverySectionComponent);
    fixture.componentRef.setInput('projectId', projectId);
    fixture.detectChanges();

    httpMock.expectOne(`/api/projects/${projectId}/agile-delivery`).flush(
      { message: 'Agile-Daten nicht verfügbar' },
      { status: 503, statusText: 'Unavailable' },
    );
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Agile-Daten nicht verfügbar');
    fixture.nativeElement.querySelector('button')?.click();
    httpMock.expectOne(`/api/projects/${projectId}/agile-delivery`).flush(payload);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('.agile-delivery__sprint').length).toBe(3);
  });
});
