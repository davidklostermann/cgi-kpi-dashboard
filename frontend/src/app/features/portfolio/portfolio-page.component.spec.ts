import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { PortfolioPageComponent } from './portfolio-page.component';
import { PortfolioKpiSummary } from '../../shared/models/portfolio-kpi.model';
import { PortfolioFilterOptions } from '../../shared/models/portfolio-filter.model';

const mockSummary: PortfolioKpiSummary = {
  activeProjectCount: 19,
  averageProgressPercent: 47.2,
  budgetDeviationPercent: 2.1,
  scheduleCompliancePercent: 63.2,
  criticalRiskCount: 4,
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
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Portfolio');
    expect(fixture.nativeElement.querySelector('app-portfolio-filter-bar')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('app-portfolio-kpi-section')).toBeTruthy();
    expect(fixture.nativeElement.querySelectorAll('app-kpi-card').length).toBe(6);
    expect(fixture.nativeElement.textContent).toContain('KI-Einschätzung');
  });
});
