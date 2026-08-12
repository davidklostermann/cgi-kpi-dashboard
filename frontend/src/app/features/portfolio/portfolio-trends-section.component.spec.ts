import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { PortfolioTrendsSectionComponent } from './portfolio-trends-section.component';
import { PortfolioFilterService } from './portfolio-filter.service';
import { PortfolioTrends } from '../../shared/models/portfolio-trends.model';

const mockTrends: PortfolioTrends = {
  points: [
    { period: '2026-06', averageProgressPercent: 42.5, totalActualBudget: 1_200_000 },
    { period: '2026-07', averageProgressPercent: 47.2, totalActualBudget: 1_350_000 },
  ],
  statusDistribution: { onTrack: 9, atRisk: 6, critical: 4, completed: 0 },
  empty: false,
};

describe('PortfolioTrendsSectionComponent', () => {
  let httpMock: HttpTestingController;
  let filterService: PortfolioFilterService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PortfolioTrendsSectionComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    filterService = TestBed.inject(PortfolioFilterService);
    filterService.reset();
  });

  afterEach(() => {
    httpMock.match('/api/portfolio/trends').forEach((req) => req.flush(mockTrends));
    httpMock.verify();
  });

  it('should render trend chart from backend data (Story 5.4)', () => {
    const fixture = TestBed.createComponent(PortfolioTrendsSectionComponent);
    fixture.detectChanges();

    httpMock.expectOne('/api/portfolio/trends').flush(mockTrends);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('app-trend-chart')).toBeTruthy();
  });

  it('should reload trends when filters change (Story 5.4)', () => {
    const fixture = TestBed.createComponent(PortfolioTrendsSectionComponent);
    fixture.detectChanges();
    httpMock.expectOne('/api/portfolio/trends').flush(mockTrends);
    fixture.detectChanges();

    filterService.update({ customer: 'Acme' });
    fixture.detectChanges();

    const filteredReq = httpMock.expectOne(
      (request) =>
        request.url.includes('/api/portfolio/trends') && request.params.get('customer') === 'Acme',
    );
    filteredReq.flush(mockTrends);
  });
});
