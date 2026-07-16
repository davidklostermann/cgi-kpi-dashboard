import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { PortfolioGanttSectionComponent } from './portfolio-gantt-section.component';
import { PortfolioFilterService } from './portfolio-filter.service';
import { PortfolioTimeline } from '../../shared/models/portfolio-timeline.model';

const mockTimeline: PortfolioTimeline = {
  projects: [
    {
      id: 'a0000000-0000-4000-8000-000000000001',
      name: 'Nexus Analytics Pilot',
      startDate: '2025-03-01',
      plannedEndDate: '2026-06-30',
      forecastEndDate: null,
      actualEndDate: null,
      scheduleDeviationDays: 0,
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      phases: [],
      milestones: [],
    },
  ],
  empty: false,
};

describe('PortfolioGanttSectionComponent', () => {
  let httpMock: HttpTestingController;
  let filterService: PortfolioFilterService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PortfolioGanttSectionComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    filterService = TestBed.inject(PortfolioFilterService);
    filterService.reset();
  });

  afterEach(() => {
    httpMock.match('/api/portfolio/timeline').forEach((req) => req.flush(mockTimeline));
    httpMock.verify();
  });

  it('should render gantt timeline from backend data (Story 5.2)', () => {
    const fixture = TestBed.createComponent(PortfolioGanttSectionComponent);
    fixture.detectChanges();

    const req = httpMock.expectOne('/api/portfolio/timeline');
    req.flush(mockTimeline);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('app-gantt-timeline')).toBeTruthy();
  });

  it('should reload timeline when filters change (Story 5.2)', () => {
    const fixture = TestBed.createComponent(PortfolioGanttSectionComponent);
    fixture.detectChanges();
    httpMock.expectOne('/api/portfolio/timeline').flush(mockTimeline);
    fixture.detectChanges();

    filterService.update({ customer: 'Acme' });
    fixture.detectChanges();

    const filteredReq = httpMock.expectOne(
      (request) =>
        request.url.includes('/api/portfolio/timeline') && request.params.get('customer') === 'Acme',
    );
    filteredReq.flush(mockTimeline);
  });
});
