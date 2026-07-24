import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { computed, signal, Signal, WritableSignal } from '@angular/core';

import { PortfolioPageComponent } from './portfolio-page.component';
import { AuthService } from '../../core/auth/auth.service';
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
  let isAdminSignal: WritableSignal<boolean>;

  beforeEach(async () => {
    isAdminSignal = signal(true);
    const authServiceMock = {
      currentUser: signal({ id: 'admin-user', roles: ['ROLE_ADMIN'] }),
      isAdmin: computed(() => isAdminSignal()) as Signal<boolean>,
    };

    await TestBed.configureTestingModule({
      imports: [PortfolioPageComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceMock },
      ],
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
    httpMock.match('/api/me/ai/readiness').forEach((req) => req.flush({ ready: true }));
    httpMock.verify();
  });

  it('should create without injecting HttpClient in page component (AD-10)', () => {
    const fixture = TestBed.createComponent(PortfolioPageComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should render the full-width dashboard with the AI drawer initially closed', () => {
    const fixture = TestBed.createComponent(PortfolioPageComponent);
    fixture.detectChanges();

    httpMock.expectOne('/api/portfolio/filters/options').flush(mockOptions);
    httpMock.expectOne('/api/portfolio/kpis').flush(mockSummary);
    httpMock.expectOne('/api/portfolio/timeline').flush(mockTimeline);
    httpMock.expectOne('/api/portfolio/projects').flush(mockTable);
    httpMock.expectOne('/api/portfolio/trends').flush(mockTrends);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Portfolio');
    expect(fixture.nativeElement.querySelector('app-portfolio-filter-bar')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('app-portfolio-kpi-section')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('app-portfolio-trends-section')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.portfolio-main')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.portfolio-main__visualizations')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('app-trend-chart')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('app-portfolio-gantt-section')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.trend-chart__grid')).toBeTruthy();
    expect(fixture.nativeElement.querySelectorAll('.trend-chart__panel').length).toBe(3);
    expect(fixture.nativeElement.querySelector('.gantt-timeline__frame')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('app-portfolio-table-section')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('app-project-table')).toBeTruthy();
    expect(fixture.nativeElement.querySelectorAll('app-kpi-card').length).toBe(5);
    expect(fixture.nativeElement.querySelector('app-facts-ai-layout')).toBeFalsy();
    expect(fixture.nativeElement.querySelector('.portfolio-ai-drawer')).toBeFalsy();
    expect(fixture.nativeElement.querySelector('.portfolio-ai-launcher')).toBeTruthy();
  });

  it('should open and close the AI drawer while restoring focus to its launcher', () => {
    const fixture = TestBed.createComponent(PortfolioPageComponent);
    fixture.detectChanges();

    httpMock.expectOne('/api/portfolio/filters/options').flush(mockOptions);
    httpMock.expectOne('/api/portfolio/kpis').flush(mockSummary);
    httpMock.expectOne('/api/portfolio/timeline').flush(mockTimeline);
    httpMock.expectOne('/api/portfolio/projects').flush(mockTable);
    httpMock.expectOne('/api/portfolio/trends').flush(mockTrends);
    fixture.detectChanges();

    const main = fixture.nativeElement.querySelector('.portfolio-main') as HTMLElement;
    const launcher = fixture.nativeElement.querySelector(
      '.portfolio-ai-launcher',
    ) as HTMLButtonElement;
    expect(main).toBeTruthy();
    expect(main.contains(fixture.nativeElement.querySelector('app-portfolio-kpi-section'))).toBe(
      true,
    );
    expect(main.contains(fixture.nativeElement.querySelector('app-portfolio-trends-section'))).toBe(
      true,
    );
    expect(main.contains(fixture.nativeElement.querySelector('app-portfolio-gantt-section'))).toBe(
      true,
    );
    expect(main.contains(fixture.nativeElement.querySelector('app-portfolio-table-section'))).toBe(
      true,
    );
    launcher.focus();
    launcher.click();
    fixture.detectChanges();

    httpMock.expectOne('/api/me/ai/readiness').flush({ ready: true });
    httpMock.expectOne('/api/portfolio/ai/trend-analysis').flush({
      insights: [],
      aiGenerated: true,
      disclaimer: 'Disclaimer',
      generatedAt: '2026-07-16T12:00:00Z',
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.portfolio-ai-drawer')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('app-portfolio-ai-panel')).toBeTruthy();
    expect(document.body.style.overflow).toBe('hidden');

    (
      fixture.nativeElement.querySelector('.portfolio-ai-drawer__close') as HTMLButtonElement
    ).click();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.portfolio-ai-drawer')).toBeFalsy();
    expect(document.activeElement).toBe(launcher);
    expect(document.body.style.overflow).toBe('');
  });

  it('should close the AI drawer when Escape is pressed', () => {
    isAdminSignal.set(true);
    const fixture = TestBed.createComponent(PortfolioPageComponent);
    fixture.detectChanges();

    httpMock.expectOne('/api/portfolio/filters/options').flush(mockOptions);
    httpMock.expectOne('/api/portfolio/kpis').flush(mockSummary);
    httpMock.expectOne('/api/portfolio/timeline').flush(mockTimeline);
    httpMock.expectOne('/api/portfolio/projects').flush(mockTable);
    httpMock.expectOne('/api/portfolio/trends').flush(mockTrends);
    fixture.detectChanges();

    const launcher = fixture.nativeElement.querySelector(
      '.portfolio-ai-launcher',
    ) as HTMLButtonElement;
    launcher.click();
    fixture.detectChanges();

    httpMock.expectOne('/api/me/ai/readiness').flush({ ready: true });
    httpMock.expectOne('/api/portfolio/ai/trend-analysis').flush({
      insights: [],
      aiGenerated: true,
      disclaimer: 'Disclaimer',
      generatedAt: '2026-07-16T12:00:00Z',
    });
    fixture.detectChanges();

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.portfolio-ai-drawer')).toBeFalsy();
  });

  it('should hide the AI launcher for USER users', () => {
    isAdminSignal.set(false);
    const fixture = TestBed.createComponent(PortfolioPageComponent);
    fixture.detectChanges();

    httpMock.expectOne('/api/portfolio/filters/options').flush(mockOptions);
    httpMock.expectOne('/api/portfolio/kpis').flush(mockSummary);
    httpMock.expectOne('/api/portfolio/timeline').flush(mockTimeline);
    httpMock.expectOne('/api/portfolio/projects').flush(mockTable);
    httpMock.expectOne('/api/portfolio/trends').flush(mockTrends);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.portfolio-ai-launcher')).toBeNull();
    expect(fixture.nativeElement.querySelector('.portfolio-ai-drawer')).toBeNull();
  });

  it('should hide the AI launcher when no authenticated user is available', () => {
    isAdminSignal.set(false);
    const fixture = TestBed.createComponent(PortfolioPageComponent);
    fixture.detectChanges();

    httpMock.expectOne('/api/portfolio/filters/options').flush(mockOptions);
    httpMock.expectOne('/api/portfolio/kpis').flush(mockSummary);
    httpMock.expectOne('/api/portfolio/timeline').flush(mockTimeline);
    httpMock.expectOne('/api/portfolio/projects').flush(mockTable);
    httpMock.expectOne('/api/portfolio/trends').flush(mockTrends);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.portfolio-ai-launcher')).toBeNull();
  });
});
