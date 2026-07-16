import { Component, computed, input, signal } from '@angular/core';

import { PortfolioStatusDistribution } from '../models/portfolio-kpi.model';
import {
  PortfolioTrendPoint,
  TrendHorizon,
} from '../models/portfolio-trends.model';

interface ChartPoint {
  period: string;
  periodLabel: string;
  x: number;
  y: number;
  valueLabel: string;
  showAxisLabel: boolean;
}

interface TrendChartModel {
  width: number;
  height: number;
  chartPoints: ChartPoint[];
  polyline: string;
  yTicks: { value: string; y: number }[];
  padding: { top: number; right: number; bottom: number; left: number };
}

/** Beschriftete, CGI-konforme Portfolio-Trendcharts ohne Donut (FR-3 / Story 5.4). */
@Component({
  selector: 'app-trend-chart',
  imports: [],
  templateUrl: './trend-chart.component.html',
  styleUrl: './trend-chart.component.scss',
})
export class TrendChartComponent {
  readonly points = input.required<PortfolioTrendPoint[]>();
  readonly statusDistribution = input.required<PortfolioStatusDistribution>();

  readonly horizon = signal<TrendHorizon>('6M');
  readonly horizons: readonly TrendHorizon[] = ['3M', '6M', '12M'];

  readonly requestedMonthCount = computed(() =>
    this.monthCountFor(this.horizon()),
  );

  readonly filteredPoints = computed(() => {
    const points = [...this.points()].sort((a, b) =>
      a.period.localeCompare(b.period),
    );
    if (points.length === 0) {
      return points;
    }

    const latest = points[points.length - 1].period;
    const cutoff = this.shiftMonth(latest, -this.requestedMonthCount() + 1);
    return points.filter((point) => point.period >= cutoff);
  });

  readonly historyNotice = computed(() => {
    const available = this.filteredPoints().length;
    const requested = this.requestedMonthCount();
    if (available >= requested) {
      return null;
    }
    return `Für den gewählten ${requested}-Monats-Zeitraum liegen aktuell ${available} Monatsstände vor.`;
  });

  readonly progressChart = computed(() =>
    this.buildChart(
      this.filteredPoints(),
      (point) => point.averageProgressPercent,
      0,
      100,
      (value) =>
        `${value.toLocaleString('de-DE', { maximumFractionDigits: 0 })} %`,
    ),
  );

  readonly budgetChart = computed(() => {
    const points = this.filteredPoints();
    const maxBudget = Math.max(
      ...points.map((point) => point.totalActualBudget),
      1,
    );
    return this.buildChart(
      points,
      (point) => point.totalActualBudget,
      0,
      this.roundBudgetAxis(maxBudget * 1.08),
      (value) => this.formatBudget(value),
    );
  });

  readonly progressHeadline = computed(() => {
    const points = this.filteredPoints();
    if (points.length === 0) {
      return { value: '—', delta: null };
    }
    const first = points[0].averageProgressPercent;
    const last = points[points.length - 1].averageProgressPercent;
    return {
      value: `${last.toLocaleString('de-DE', { minimumFractionDigits: 1, maximumFractionDigits: 1 })} %`,
      delta:
        points.length > 1
          ? this.formatSigned(last - first, 'Prozentpunkte')
          : null,
    };
  });

  readonly budgetHeadline = computed(() => {
    const points = this.filteredPoints();
    if (points.length === 0) {
      return { value: '—', delta: null };
    }
    const first = points[0].totalActualBudget;
    const last = points[points.length - 1].totalActualBudget;
    return {
      value: this.formatBudget(last),
      delta: points.length > 1 ? this.formatSignedBudget(last - first) : null,
    };
  });

  readonly statusBars = computed(() => {
    const distribution = this.statusDistribution();
    const items = [
      {
        key: 'onTrack',
        label: 'Auf Kurs',
        count: distribution.onTrack,
        className: 'on-track',
      },
      {
        key: 'atRisk',
        label: 'Beobachten',
        count: distribution.atRisk,
        className: 'at-risk',
      },
      {
        key: 'critical',
        label: 'Kritisch',
        count: distribution.critical,
        className: 'critical',
      },
      {
        key: 'completed',
        label: 'Abgeschlossen',
        count: distribution.completed,
        className: 'completed',
      },
    ];
    const total = items.reduce((sum, item) => sum + item.count, 0);
    return items.map((item) => ({
      ...item,
      widthPercent: total > 0 ? (item.count / total) * 100 : 0,
      percentageLabel:
        total > 0
          ? `${((item.count / total) * 100).toLocaleString('de-DE', { maximumFractionDigits: 0 })} %`
          : '0 %',
    }));
  });

  readonly totalProjects = computed(() =>
    this.statusBars().reduce((sum, item) => sum + item.count, 0),
  );

  readonly screenReaderSummary = computed(() => {
    const points = this.filteredPoints();
    if (points.length === 0) {
      return 'Keine Trenddaten im gewählten Zeitraum.';
    }
    const first = points[0];
    const last = points[points.length - 1];
    const dist = this.statusDistribution();
    return (
      `Fortschritt ${this.formatPeriod(first.period)}: ${first.averageProgressPercent.toLocaleString('de-DE')} Prozent, ` +
      `${this.formatPeriod(last.period)}: ${last.averageProgressPercent.toLocaleString('de-DE')} Prozent. ` +
      `Ist-Kosten ${this.formatPeriod(first.period)}: ${this.formatBudget(first.totalActualBudget)}, ` +
      `${this.formatPeriod(last.period)}: ${this.formatBudget(last.totalActualBudget)}. ` +
      `Statusverteilung: Auf Kurs ${dist.onTrack}, Beobachten ${dist.atRisk}, ` +
      `Kritisch ${dist.critical}, Abgeschlossen ${dist.completed}.`
    );
  });

  setHorizon(value: TrendHorizon): void {
    this.horizon.set(value);
  }

  private buildChart(
    points: PortfolioTrendPoint[],
    valueSelector: (point: PortfolioTrendPoint) => number,
    min: number,
    max: number,
    valueFormatter: (value: number) => string,
  ): TrendChartModel {
    const width = 440;
    const height = 220;
    const padding = { top: 22, right: 18, bottom: 46, left: 64 };
    const plotWidth = width - padding.left - padding.right;
    const plotHeight = height - padding.top - padding.bottom;
    const range = Math.max(max - min, 1);
    const axisLabelStep = points.length > 8 ? 2 : 1;

    const chartPoints: ChartPoint[] = points.map((point, index) => {
      const value = valueSelector(point);
      const x =
        padding.left +
        (points.length === 1
          ? plotWidth / 2
          : (index / (points.length - 1)) * plotWidth);
      const y = padding.top + plotHeight - ((value - min) / range) * plotHeight;
      return {
        period: point.period,
        periodLabel: this.formatPeriod(point.period),
        x,
        y,
        valueLabel: valueFormatter(value),
        showAxisLabel:
          index % axisLabelStep === 0 || index === points.length - 1,
      };
    });

    const polyline = chartPoints
      .map((point) => `${point.x},${point.y}`)
      .join(' ');
    const yTicks = [min, min + range / 2, max].map((tick) => ({
      value: valueFormatter(tick),
      y: padding.top + plotHeight - ((tick - min) / range) * plotHeight,
    }));

    return { width, height, chartPoints, polyline, yTicks, padding };
  }

  private formatPeriod(period: string): string {
    const [year, month] = period.split('-').map(Number);
    const date = new Date(year, month - 1, 1);
    return date.toLocaleDateString('de-DE', {
      month: 'short',
      year: '2-digit',
    });
  }

  private formatBudget(value: number): string {
    if (Math.abs(value) >= 1_000_000) {
      return `${(value / 1_000_000).toLocaleString('de-DE', { maximumFractionDigits: 1 })} Mio. €`;
    }
    if (Math.abs(value) >= 1_000) {
      return `${(value / 1_000).toLocaleString('de-DE', { maximumFractionDigits: 0 })} Tsd. €`;
    }
    return `${value.toLocaleString('de-DE', { maximumFractionDigits: 0 })} €`;
  }

  private formatSigned(value: number, unit: string): string {
    const sign = value > 0 ? '+' : value < 0 ? '−' : '±';
    return `${sign}${Math.abs(value).toLocaleString('de-DE', { maximumFractionDigits: 1 })} ${unit}`;
  }

  private formatSignedBudget(value: number): string {
    const sign = value > 0 ? '+' : value < 0 ? '−' : '±';
    return `${sign}${this.formatBudget(Math.abs(value))}`;
  }

  private roundBudgetAxis(value: number): number {
    if (value <= 0) {
      return 1;
    }
    const magnitude = 10 ** Math.floor(Math.log10(value));
    return Math.ceil(value / magnitude) * magnitude;
  }

  private monthCountFor(horizon: TrendHorizon): number {
    return horizon === '3M' ? 3 : horizon === '6M' ? 6 : 12;
  }

  private shiftMonth(period: string, delta: number): string {
    const [year, month] = period.split('-').map(Number);
    const date = new Date(year, month - 1 + delta, 1);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
  }
}
