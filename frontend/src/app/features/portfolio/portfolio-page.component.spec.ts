import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { PortfolioPageComponent } from './portfolio-page.component';
import { PortfolioKpiSummary } from '../../shared/models/portfolio-kpi.model';
import { PortfolioFilterOptions } from '../../shared/models/portfolio-filter.model';
import { PortfolioTimeline } from '../../shared/models/portfolio-timeline.model';
import { PortfolioTable } from '../../shared/models/portfolio-table.model';
import { PortfolioTrends } from '../../shared/models/portfolio-trends.model';

const mockSummary: PortfolioKpiSummary = {
  activeProjectCount: 19,
  averageProgressPercent: 47.2,
  budgetDeviationPercent: 2.1,
  scheduleCompliancePercent: 63.2,
  criticalRiskCount: 4,
  statusDistribution: { onTrack: 9, atRisk: 6, critical: 4, completed: 0 },
  empty: false,
};

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
      phases: [
        {
          name: 'Umsetzung',
          phaseType: 'UMSETZUNG',
          startDate: '2025-09-01',
          endDate: '2026-06-30',
          sortOrder: 2,
        },
      ],
      milestones: [],
    },
  ],
  empty: false,
};

const mockTable: PortfolioTable = {
  projects: [
    {
      id: 'a0000000-0000-4000-8000-000000000001',
      name: 'Nexus Analytics Pilot',
      customerName: 'Acme GmbH',
      projectLead: 'Dr. Anna Keller',
      status: 'ON_TRACK',
      statusLabel: 'Auf Kurs',
      currentPhaseName: 'Umsetzung',
      progressPercent: 62,
      plannedEndDate: '2026-06-30',
      forecastEndDate: null,
      scheduleDeviationDays: 0,
      budgetUtilizationPercent: 100,
      budgetDeviationPercent: 0,
      effortDeviationPercent: 0,
      openRiskCount: 0,
      criticalIssueCount: 0,
      lastDataUpdate: '2026-07-10T08:00:00Z',
    },
  ],
  empty: false,
};

const mockTrends: PortfolioTrends = {
  points: [
    { period: '2026-06', averageProgressPercent: 42.5, totalActualBudget: 1_200_000 },
    { period: '2026-07', averageProgressPercent: 47.2, totalActualBudget: 1_350_000 },
  ],
  statusDistribution: { onTrack: 9, atRisk: 6, critical: 4, completed: 0 },
  empty: false,
};

const mockOptions: PortfolioFilterOptions = {
  customers: ['Gamma Industries KG'],
  projectLeads: ['Dr. Anna Keller'],
  phases: ['Umsetzung'],
  reportMonths: ['2026-07'],
  statuses: ['ON_TRACK', 'CRITICAL'],
  riskSeverities: ['HIGH'],
};

describe('PortfolioPageComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PortfolioPageComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.match('/api/portfolio/filters/options').forEach((req) => req.flush(mockOptions));
    httpMock.match('/api/portfolio/kpis').forEach((req) => req.flush(mockSummary));
    httpMock.match('/api/portfolio/timeline').forEach((req) => req.flush(mockTimeline));
    httpMock.match('/api/portfolio/projects').forEach((req) => req.flush(mockTable));
    httpMock.match('/api/portfolio/trends').forEach((req) => req.flush(mockTrends));
    httpMock.match('/api/portfolio/ai/trend-analysis').forEach((req) =>
      req.flush({
        text: 'Trendanalyse',
        aiGenerated: true,
        disclaimer: 'Disclaimer',
        generatedAt: '2026-07-16T12:00:00Z',
        insights: [],
      }),
    );
    httpMock.verify();
  });

  it('should create without injecting HttpClient in page component (AD-10)', () => {
    const fixture = TestBed.createComponent(PortfolioPageComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should render filter bar, KPI section and facts-ai layout (Story 4.4)', () => {
    const fixture = TestBed.createComponent(PortfolioPageComponent);
    fixture.detectChanges();

    httpMock.expectOne('/api/portfolio/filters/options').flush(mockOptions);
    httpMock.expectOne('/api/portfolio/kpis').flush(mockSummary);
    httpMock.expectOne('/api/portfolio/timeline').flush(mockTimeline);
    httpMock.expectOne('/api/portfolio/projects').flush(mockTable);
    httpMock.expectOne('/api/portfolio/trends').flush(mockTrends);
    httpMock.expectOne('/api/portfolio/ai/trend-analysis').flush({
      insights: [],
      aiGenerated: true,
      disclaimer: 'Disclaimer',
      generatedAt: '2026-07-16T12:00:00Z',
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Portfolio');
    expect(fixture.nativeElement.querySelector('app-portfolio-filter-bar')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('app-portfolio-kpi-section')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('app-portfolio-trends-section')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.page__visualizations')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('app-trend-chart')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('app-portfolio-gantt-section')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('app-portfolio-table-section')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('app-project-table')).toBeTruthy();
    expect(fixture.nativeElement.querySelectorAll('app-kpi-card').length).toBe(5);
    expect(fixture.nativeElement.querySelector('app-portfolio-ai-panel')).toBeTruthy();
    expect(fixture.nativeElement.textContent).toContain('Portfolio-Muster und systemische Risiken');
  });
});
