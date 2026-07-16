import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { firstValueFrom } from 'rxjs';

import { PortfolioApiService } from './portfolio-api.service';

import { PortfolioKpiSummary } from '../../shared/models/portfolio-kpi.model';

describe('PortfolioApiService', () => {
  let service: PortfolioApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(PortfolioApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('getPortfolioKpis should call /api/portfolio/kpis', async () => {
    const mockSummary: PortfolioKpiSummary = {
      activeProjectCount: 19,
      averageProgressPercent: 47.2,
      budgetDeviationPercent: 2.1,
      scheduleCompliancePercent: 63.2,
      criticalRiskCount: 4,
      statusDistribution: { onTrack: 9, atRisk: 6, critical: 4, completed: 0 },
      empty: false,
    };

    const responsePromise = firstValueFrom(service.getPortfolioKpis());

    const req = httpMock.expectOne('/api/portfolio/kpis');
    expect(req.request.method).toBe('GET');
    req.flush(mockSummary);

    await expect(responsePromise).resolves.toEqual(mockSummary);
  });

  it('getPortfolioKpis should forward filter query params (Story 4.4)', async () => {
    const responsePromise = firstValueFrom(
      service.getPortfolioKpis({ customer: 'Gamma', status: ['CRITICAL'] }),
    );

    const req = httpMock.expectOne((request) =>
      request.url === '/api/portfolio/kpis' &&
      request.params.get('customer') === 'Gamma' &&
      (request.params.getAll('status') ?? []).includes('CRITICAL'),
    );
    req.flush({
      activeProjectCount: 1,
      averageProgressPercent: 78,
      budgetDeviationPercent: 0,
      scheduleCompliancePercent: 0,
      criticalRiskCount: 0,
      statusDistribution: { onTrack: 0, atRisk: 0, critical: 1, completed: 0 },
      empty: false,
    });

    await expect(responsePromise).resolves.toMatchObject({ activeProjectCount: 1 });
  });

  it('getHealthProbe should call /actuator/health without /api prefix', async () => {
    const responsePromise = firstValueFrom(service.getHealthProbe());

    const req = httpMock.expectOne('/actuator/health');
    expect(req.request.method).toBe('GET');
    req.flush({ status: 'UP' });

    await expect(responsePromise).resolves.toEqual({ status: 'UP' });
  });
});
