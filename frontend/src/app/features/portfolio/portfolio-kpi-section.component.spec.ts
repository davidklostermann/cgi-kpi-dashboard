import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { PortfolioKpiSectionComponent } from './portfolio-kpi-section.component';
import { PortfolioFilterService } from './portfolio-filter.service';
import { PortfolioKpiSummary } from '../../shared/models/portfolio-kpi.model';

const mockSummary: PortfolioKpiSummary = {
  activeProjectCount: 19,
  averageProgressPercent: 47.2,
  budgetDeviationPercent: 2.1,
  scheduleCompliancePercent: 63.2,
  criticalRiskCount: 4,
  statusDistribution: { onTrack: 9, atRisk: 6, critical: 4, completed: 0 },
  empty: false,
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

describe('PortfolioKpiSectionComponent', () => {
  let httpMock: HttpTestingController;
  let filterService: PortfolioFilterService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PortfolioKpiSectionComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    filterService = TestBed.inject(PortfolioFilterService);
    filterService.reset();
  });

  afterEach(() => {
    httpMock.match('/api/portfolio/kpis').forEach((req) => req.flush(mockSummary));
    httpMock.verify();
  });

  it('should render KPI cards from backend data (Story 4.3)', () => {
    const fixture = TestBed.createComponent(PortfolioKpiSectionComponent);
    fixture.detectChanges();

    const req = httpMock.expectOne('/api/portfolio/kpis');
    expect(req.request.method).toBe('GET');
    req.flush(mockSummary);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelectorAll('app-kpi-card').length).toBe(5);
  });

  it('should reload KPIs when filters change (Story 4.4)', () => {
    const fixture = TestBed.createComponent(PortfolioKpiSectionComponent);
    fixture.detectChanges();
    httpMock.expectOne('/api/portfolio/kpis').flush(mockSummary);
    fixture.detectChanges();

    filterService.update({ customer: 'Gamma' });
    fixture.detectChanges();

    const filteredReq = httpMock.expectOne((request) =>
      request.url.includes('/api/portfolio/kpis') && request.params.get('customer') === 'Gamma',
    );
    filteredReq.flush(mockSummary);
    fixture.detectChanges();

    expect(filteredReq.request.params.get('customer')).toBe('Gamma');
  });

  it('should show filtered empty state with reset action (Story 4.4)', () => {
    filterService.update({ customer: 'NichtVorhanden' });

    const fixture = TestBed.createComponent(PortfolioKpiSectionComponent);
    fixture.detectChanges();

    const req = httpMock.expectOne((request) =>
      request.url.includes('/api/portfolio/kpis') && request.params.get('customer') === 'NichtVorhanden',
    );
    req.flush(emptySummary);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Keine Projekte für diesen Filter');
    expect(fixture.nativeElement.textContent).toContain('Filter zurücksetzen');
    expect(fixture.nativeElement.querySelectorAll('app-kpi-card').length).toBe(0);
  });

  it('should label critical risks distinctly from table issues (Epic 5 review)', () => {
    const fixture = TestBed.createComponent(PortfolioKpiSectionComponent);
    fixture.detectChanges();
    httpMock.expectOne('/api/portfolio/kpis').flush(mockSummary);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Kritische Risiken (offen)');
    expect(fixture.nativeElement.textContent).not.toContain('Ampelverteilung');
  });

  it('should show error panel only in KPI section and retry on failure (Story 4.3)', () => {
    const fixture = TestBed.createComponent(PortfolioKpiSectionComponent);
    fixture.detectChanges();

    const firstReq = httpMock.expectOne('/api/portfolio/kpis');
    firstReq.flush({ code: 'INTERNAL_ERROR', message: 'Serverfehler beim Laden.' }, { status: 500, statusText: 'Error' });
    fixture.detectChanges();

    const retryButton = fixture.nativeElement.querySelector('.portfolio-kpi-section__retry') as HTMLButtonElement;
    retryButton.click();
    fixture.detectChanges();

    const retryReq = httpMock.expectOne('/api/portfolio/kpis');
    retryReq.flush(mockSummary);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelectorAll('app-kpi-card').length).toBe(5);
  });
});
