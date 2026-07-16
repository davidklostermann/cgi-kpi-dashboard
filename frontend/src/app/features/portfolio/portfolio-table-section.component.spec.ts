import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { PortfolioTableSectionComponent } from './portfolio-table-section.component';
import { PortfolioFilterService } from './portfolio-filter.service';
import { PortfolioTable } from '../../shared/models/portfolio-table.model';

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

describe('PortfolioTableSectionComponent', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PortfolioTableSectionComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    TestBed.inject(PortfolioFilterService).reset();
  });

  afterEach(() => {
    httpMock.match('/api/portfolio/projects').forEach((req) => req.flush(mockTable));
    httpMock.verify();
  });

  it('should render project table from backend data (Story 5.3)', () => {
    const fixture = TestBed.createComponent(PortfolioTableSectionComponent);
    fixture.detectChanges();

    httpMock.expectOne('/api/portfolio/projects').flush(mockTable);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('app-project-table')).toBeTruthy();
  });
});
