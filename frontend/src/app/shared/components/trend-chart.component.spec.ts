import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { TrendChartComponent } from './trend-chart.component';
import { PortfolioTrendPoint } from '../models/portfolio-trends.model';
import { PortfolioStatusDistribution } from '../models/portfolio-kpi.model';

const mockPoints: PortfolioTrendPoint[] = [
  {
    period: '2026-06',
    averageProgressPercent: 42.5,
    totalActualBudget: 1_200_000,
  },
  {
    period: '2026-07',
    averageProgressPercent: 47.2,
    totalActualBudget: 1_350_000,
  },
];

const mockDistribution: PortfolioStatusDistribution = {
  onTrack: 9,
  atRisk: 6,
  critical: 4,
  completed: 0,
};

@Component({
  selector: 'app-test-host',
  imports: [TrendChartComponent],
  template: `<app-trend-chart
    [points]="points"
    [statusDistribution]="distribution"
  />`,
})
class TestHostComponent {
  readonly points = mockPoints;
  readonly distribution = mockDistribution;
}

describe('TrendChartComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
    }).compileComponents();
  });

  it('should render CGI-styled progress and budget trend charts (Story 5.4)', () => {
    const fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();

    const charts = fixture.nativeElement.querySelectorAll(
      '.trend-chart__svg',
    ) as NodeListOf<SVGElement>;
    expect(charts.length).toBe(2);
    charts.forEach((chart) =>
      expect(chart.getAttribute('viewBox')).toBe('0 0 440 160'),
    );
    expect(fixture.nativeElement.textContent).toContain(
      'Portfolio-Fortschritt',
    );
    expect(fixture.nativeElement.textContent).toContain(
      'Ist-Kosten des Portfolios',
    );
    expect(
      fixture.nativeElement.querySelector('.trend-chart__line--progress'),
    ).toBeTruthy();
    expect(
      fixture.nativeElement.querySelector('.trend-chart__line--budget'),
    ).toBeTruthy();
  });

  it('should render status distribution as labeled bars, not a donut (Story 5.4)', () => {
    const fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector('.trend-chart__status-bars'),
    ).toBeTruthy();
    expect(fixture.nativeElement.textContent).toContain('Auf Kurs');
    expect(fixture.nativeElement.textContent).toContain('Beobachten');
    expect(fixture.nativeElement.textContent).toContain('Kritisch');
    expect(
      fixture.nativeElement.querySelector('circle[role="img"]'),
    ).toBeNull();
  });

  it('should expose screen-reader summary and aria-pressed horizon buttons', () => {
    const fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();

    const summary = fixture.nativeElement.querySelector(
      '#trend-chart-summary',
    ) as HTMLElement;
    expect(summary.classList.contains('visually-hidden')).toBe(true);
    expect(summary.textContent).toContain('Fortschritt');

    const active = fixture.nativeElement.querySelector(
      '.trend-chart__horizon-btn--active',
    ) as HTMLButtonElement;
    expect(active.getAttribute('aria-pressed')).toBe('true');
  });

  it('should filter visible months by horizon (Epic 5 review)', () => {
    @Component({
      selector: 'app-horizon-host',
      imports: [TrendChartComponent],
      template: `<app-trend-chart
        [points]="points"
        [statusDistribution]="distribution"
      />`,
    })
    class HorizonHostComponent {
      readonly points: PortfolioTrendPoint[] = [
        {
          period: '2025-08',
          averageProgressPercent: 20,
          totalActualBudget: 800_000,
        },
        {
          period: '2025-09',
          averageProgressPercent: 22,
          totalActualBudget: 820_000,
        },
        {
          period: '2025-10',
          averageProgressPercent: 24,
          totalActualBudget: 840_000,
        },
        {
          period: '2025-11',
          averageProgressPercent: 26,
          totalActualBudget: 860_000,
        },
        {
          period: '2025-12',
          averageProgressPercent: 28,
          totalActualBudget: 880_000,
        },
        {
          period: '2026-01',
          averageProgressPercent: 30,
          totalActualBudget: 900_000,
        },
        {
          period: '2026-02',
          averageProgressPercent: 32,
          totalActualBudget: 920_000,
        },
        {
          period: '2026-03',
          averageProgressPercent: 34,
          totalActualBudget: 940_000,
        },
        {
          period: '2026-04',
          averageProgressPercent: 36,
          totalActualBudget: 960_000,
        },
        {
          period: '2026-05',
          averageProgressPercent: 38,
          totalActualBudget: 980_000,
        },
        {
          period: '2026-06',
          averageProgressPercent: 42.5,
          totalActualBudget: 1_200_000,
        },
        {
          period: '2026-07',
          averageProgressPercent: 47.2,
          totalActualBudget: 1_350_000,
        },
      ];
      readonly distribution = mockDistribution;
    }

    const fixture = TestBed.createComponent(HorizonHostComponent);
    fixture.detectChanges();
    const chart = fixture.debugElement.children[0]
      .componentInstance as TrendChartComponent;

    chart.setHorizon('3M');
    fixture.detectChanges();
    expect(chart.filteredPoints().length).toBe(3);
    expect(chart.filteredPoints()[0].period).toBe('2026-05');

    chart.setHorizon('12M');
    fixture.detectChanges();
    expect(chart.filteredPoints().length).toBe(12);
  });

  it('should explain insufficient historical data instead of implying a full period', () => {
    const fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain(
      'liegen aktuell 2 Monatsstände vor',
    );
  });

  it('should hide status bars when count is zero', () => {
    const fixture = TestBed.createComponent(TestHostComponent);
    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelectorAll('.trend-chart__status-bar').length,
    ).toBe(3);
    expect(fixture.nativeElement.textContent).toContain('Abgeschlossen');
  });
});
