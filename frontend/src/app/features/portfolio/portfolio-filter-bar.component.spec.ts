import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { PortfolioFilterBarComponent } from './portfolio-filter-bar.component';
import { PortfolioFilterService } from './portfolio-filter.service';
import { PortfolioKpiSummary } from '../../shared/models/portfolio-kpi.model';
import { PortfolioFilterOptions } from '../../shared/models/portfolio-filter.model';

const mockOptions: PortfolioFilterOptions = {
  customers: ['Gamma Industries KG'],
  projectLeads: ['Dr. Anna Keller'],
  phases: ['Umsetzung'],
  reportMonths: ['2026-07'],
  statuses: ['ON_TRACK', 'CRITICAL'],
  riskSeverities: ['HIGH'],
};

const emptySummary: PortfolioKpiSummary = {
  activeProjectCount: 0,
  averageProgressPercent: 0,
  budgetDeviationPercent: 0,
  scheduleCompliancePercent: 0,
  criticalRiskCount: 0,
  statusDistribution: { onTrack: 0, atRisk: 0, critical: 0, completed: 0 },
  empty: true,
};

describe('PortfolioFilterBarComponent', () => {
  let httpMock: HttpTestingController;
  let filterService: PortfolioFilterService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PortfolioFilterBarComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    filterService = TestBed.inject(PortfolioFilterService);
    filterService.reset();
  });

  afterEach(() => {
    httpMock.match('/api/portfolio/filters/options').forEach((req) => req.flush(mockOptions));
    httpMock.match('/api/portfolio/kpis').forEach((req) => req.flush(emptySummary));
    httpMock.verify();
  });

  it('should load filter options and update shared filter state (Story 4.4)', () => {
    const fixture = TestBed.createComponent(PortfolioFilterBarComponent);
    fixture.detectChanges();

    const optionsReq = httpMock.expectOne('/api/portfolio/filters/options');
    optionsReq.flush(mockOptions);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Kunde / Geschäftsbereich');
    expect(fixture.nativeElement.querySelector('.filter-bar')).toBeTruthy();

    const customerInput = fixture.nativeElement.querySelector(
      'input[list="portfolio-customers"]',
    ) as HTMLInputElement;
    customerInput.value = 'Gamma';
    customerInput.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    expect(filterService.filters().customer).toBe('Gamma');
    expect(filterService.toQueryParams()['customer']).toBe('Gamma');
  });

  it('should reset all filters from filter bar (Story 4.4)', () => {
    filterService.update({ customer: 'Gamma', statuses: ['CRITICAL'] });

    const fixture = TestBed.createComponent(PortfolioFilterBarComponent);
    fixture.detectChanges();
    httpMock.expectOne('/api/portfolio/filters/options').flush(mockOptions);
    fixture.detectChanges();

    const resetButton = fixture.nativeElement.querySelector('.filter-bar__reset') as HTMLButtonElement;
    resetButton.click();
    fixture.detectChanges();

    expect(filterService.hasActiveFilters()).toBe(false);
  });
});
